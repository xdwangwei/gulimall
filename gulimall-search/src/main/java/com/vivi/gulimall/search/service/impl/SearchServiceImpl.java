package com.vivi.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.vivi.common.constant.SearchConstant;
import com.vivi.common.to.BrandTO;
import com.vivi.common.to.SkuESModel;
import com.vivi.common.utils.R;
import com.vivi.gulimall.search.config.ProductSearchConfig;
import com.vivi.gulimall.search.feign.ProductFeignService;
import com.vivi.gulimall.search.service.SearchService;
import com.vivi.gulimall.search.vo.AttrRespVO;
import com.vivi.gulimall.search.vo.SearchParam;
import com.vivi.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author wangwei
 * 2020/10/28 16:52
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchRequest searchRequest = buildSearchRequest(param);
        SearchResult result = null;
        try {
            SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);
            result = buildSearchResult(param, response);
        } catch (IOException e) {
            log.error("检索ES失败: {}", e);
        }
        return result;
    }


    /**
     * 从前端传来的查询参数构建出 DSL 去ES进行查询
     *
     * 模糊匹配keyword，
     * 过滤(分类id。品牌id，价格区间，是否有库存，规格属性)，
     * 排序，
     * 分页，
     * 高亮，
     * 聚合分析
     *
     * GET /gulimall-product/_search
     * {
     *   "query": {
     *     "bool": {
     *       "must": [
     *         {} # keyword模糊匹配
     *       ],
     *       "filter": [
     *         {}, 分类id
     *         {}, 品牌id
     *         {}, 价格区间
     *         {}, 是否有库存，
     *         {} 规格属性
     *       ]
     *     }
     *   },
     *   "sort": [
     *     {}       排序
     *   ],
     *   "from": 0,  分页
     *   "size": 2,
     *   "highlight": {}  高亮
     *   "aggs": {}   聚合分析
     * }
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices(SearchConstant.ESIndex.ES_PRODUCT_INDEX);
        // 构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 构建bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.模糊匹配keyword
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 2.过滤(分类id。品牌id，价格区间，是否有库存，规格属性)，
        // 2.1 分类id
        if (param.getCatelog3Id() != null &&  param.getCatelog3Id() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catelogId", param.getCatelog3Id()));
        }
        // 2.2 品牌id
        List<Long> brandId = param.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }
        // 2.3 价格区间 1_500 / _500 / 500_
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
        String price = param.getSkuPrice();
        if (!StringUtils.isEmpty(price)) {
            String[] priceInfo = price.split("_");
            // 1_500
            if (priceInfo.length == 2) {
                rangeQueryBuilder.gte(priceInfo[0]).lte(priceInfo[1]);
            //    _500
            } else if (price.startsWith("_")) {
                rangeQueryBuilder.lte(priceInfo[0]);
            //    500_
            } else {
                rangeQueryBuilder.gte(priceInfo[0]);
            }
        }
        boolQueryBuilder.filter(rangeQueryBuilder);
        // 2.4 库存
        if (param.getHasStock() != null) {
            boolean flag = param.getHasStock() == 0 ? false : true;
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", flag));
        }
        // 2.5 规格属性
        // attrs=1_钢精:铝合&attrs=2_anzhuo:apple&attrs=3_lisi ==> attrs=[1_钢精:铝合,2_anzhuo:apple,3_lisi]
        List<String> attrs = param.getAttrs();
        if (!CollectionUtils.isEmpty(attrs)) {
            // 每个属性参数 attrs=1_钢精:铝合 ==》 nestedQueryFilter
            /**
             *          {
             *           "nested": {
             *             "path": "",
             *             "query": {
             *               "bool": {
             *                 "must": [
             *                   {},
             *                   {}
             *                 ]
             *               }
             *             }
             *           }
             *         },
             */
            for (String attr : attrs) {
                String[] attrInfo = attr.split("_");
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrInfo[0]));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrInfo[1].split(":")));
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }

        // 第一部分bool查询组合结束
        builder.query(boolQueryBuilder);

        // 3.排序，sort=hotScore_asc/desc
        String sortStr = param.getSort();
        if (!StringUtils.isEmpty(sortStr)) {
            String[] sortInfo = sortStr.split("_");
            SortOrder sortType = sortInfo[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(sortInfo[0], sortType);
        }

        // 4.分页，
        builder.from(param.getPageNum() == null ? 0 : (param.getPageNum() - 1) * ProductSearchConfig.PAGE_SIZE);
        builder.size(ProductSearchConfig.PAGE_SIZE);
        // 5.高亮，查询关键字不为空才有结果高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");
            builder.highlighter(highlightBuilder);
        }
        // 6.聚合分析，分析得到的商品所涉及到的分类、品牌、规格参数，
        // term值的是分布情况，就是存在哪些值，每种值下有几个数据; size是取所有结果的前几种，(按id聚合后肯定是同一种，所以可以指定为1)
        // 6.1 分类部分，按照分类id聚合，划分出分类后，每个分类内按照分类名字聚合就得到分类名，不用再根据id再去查询数据库
        TermsAggregationBuilder catelogAgg = AggregationBuilders.terms("catelogAgg").field("catelogId");
        catelogAgg.subAggregation(AggregationBuilders.terms("catelogNameAgg").field("catelogName").size(1));
        builder.aggregation(catelogAgg);
        // 6.2 分类部分，按照品牌id聚合，划分出品牌后，每个品牌内按照品牌名字聚合就得到品牌名，不用再根据id再去查询数据库
        // 每个品牌内按照品牌logo聚合就得到品牌logo，不用再根据id再去查询数据库
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg").field("brandId");
        brandAgg.subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImgAgg").field("brandImg").size(1));
        builder.aggregation(brandAgg);
        // 6.3 规格参数部分，按照规格参数id聚合，划分出规格参数后，每个品牌内按照规格参数名字聚合就得到规格参数名，不用再根据id再去查询数据库
        // 每个规格参数内按照规格参数值聚合就得到规格参数值，不用再根据id再去查询数据库
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        nestedAggregationBuilder.subAggregation(attrIdAgg);
        builder.aggregation(nestedAggregationBuilder);

        // 组和完成
        System.out.println("搜索参数构建的DSL语句：" + builder);
        searchRequest.source(builder);
        return searchRequest;
    }

    /**
     * 从ES返回的结果构造出 指定的结构数据
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchParam param, SearchResponse response) {

        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        /**
         * 全部商品数据
         */
        List<SkuESModel> esModels = Arrays.stream(hits.getHits()).map(hit -> {
            // 每个命中的记录的_source部分是真正的数据的json字符串
            String sourceAsString = hit.getSourceAsString();
            SkuESModel esModel = JSON.parseObject(sourceAsString, SkuESModel.class);
            if (!StringUtils.isEmpty(param.getKeyword())) {
                String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].toString();
                esModel.setSkuTitle(skuTitle);
            }
            return esModel;
        }).collect(Collectors.toList());
        result.setSkuList(esModels);
        /**
         * 聚合结果--分类
         */
        Aggregations aggregations = response.getAggregations();
        // debug模式下确定这个返回的具体类型
        ParsedLongTerms catelogAgg = aggregations.get("catelogAgg");
        // 每一个bucket是一种分类，有几个bucket就会有几个分类
        List<SearchResult.CatelogVO> catelogs = catelogAgg.getBuckets().stream().map(bucket -> {
            // debug查看下结果
            long catelogId = bucket.getKeyAsNumber().longValue();
            // debug模式下确定这个返回的具体类型
            ParsedStringTerms catelogNameAgg = bucket.getAggregations().get("catelogNameAgg");
            // 根据id分类后肯定是同一类，只可能有一种名字，所以直接取第一个bucket
            String catelogName = catelogNameAgg.getBuckets().get(0).getKeyAsString();
            SearchResult.CatelogVO catelogVO = new SearchResult.CatelogVO();
            catelogVO.setCatelogId(catelogId);
            catelogVO.setCatelogName(catelogName);
            return catelogVO;
        }).collect(Collectors.toList());
        result.setCatelogs(catelogs);
        /**
         * 聚合结果--品牌，与上面过程类似
         */
        ParsedLongTerms brandAgg = aggregations.get("brandAgg");
        List<SearchResult.BrandVO> brands = brandAgg.getBuckets().stream().map(bucket -> {
            long brandId = bucket.getKeyAsNumber().longValue();
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brandImgAgg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            SearchResult.BrandVO brandVO = new SearchResult.BrandVO();
            brandVO.setBrandId(brandId);
            brandVO.setBrandName(brandName);
            brandVO.setBrandImg(brandImg);
            return brandVO;
        }).collect(Collectors.toList());
        result.setBrands(brands);
        /**
         * 聚合结果--规格参数
         */
        ParsedNested attrAgg = aggregations.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResult.AttrVO> attrs = attrIdAgg.getBuckets().stream().map(bucket -> {
            long attrId = bucket.getKeyAsNumber().longValue();
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            // 根据id分类后肯定是同一类，只可能有一种名字，所以直接取第一个bucket
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            // 根据id分类后肯定是同一类，但是可以有多个值，所以会有多个bucket，把所有值组合起来
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            List<String> attrValue = attrValueAgg.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList());
            SearchResult.AttrVO attrVO = new SearchResult.AttrVO();
            attrVO.setAttrId(attrId);
            attrVO.setAttrName(attrName);
            attrVO.setAttrValue(attrValue);
            return attrVO;
        }).collect(Collectors.toList());
        result.setAttrs(attrs);
        /**
         * 分页信息
         */
        // 总记录数
        result.setTotalCount(hits.getTotalHits().value);
        // 每页大小
        result.setPageSize(ProductSearchConfig.PAGE_SIZE);
        // 总页数
        result.setTotalPage((result.getTotalCount() + ProductSearchConfig.PAGE_SIZE - 1) / ProductSearchConfig.PAGE_SIZE);
        // 当前页码
        int pageNum = param.getPageNum() == null ? 1 : param.getPageNum();
        result.setCurrPage(pageNum);
        // 构建页码导航,以当前页为中心，连续5页
        ArrayList<Integer> pageNavs = new ArrayList<>();
        for (int i = pageNum - 2; i <= pageNum + 2; ++i) {
            if (i <= 0) {
                continue;
            }
            if (i >= result.getTotalPage()) {
                break;
            }
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        List<SearchResult.BreadCrumbsVO> breadCrumbsVOS = new LinkedList<>();
        /**
         * 构建面包屑导航--参数品牌部分
         */
        List<Long> ids = param.getBrandId();
        if (!CollectionUtils.isEmpty(ids)) {
            R res = productFeignService.getBatch(ids);
            if (res.getCode() == 0) {
                List<BrandTO> brandTOS = res.getData(new TypeReference<List<BrandTO>>() {});
                brandTOS.forEach(brandTO -> {
                    SearchResult.BreadCrumbsVO crumb = new SearchResult.BreadCrumbsVO();
                    crumb.setAttrName("品牌");
                    crumb.setAttrValue(brandTO.getName());
                    // 请求参数中去掉当前属性之后的链接地址
                    String link = param.getQueryString().replace("&brandId=" + brandTO.getBrandId(), "").replace("brandId=" + brandTO.getBrandId(), "");
                    crumb.setLink("http://search.gulimall.com/list.html?" + link);
                    breadCrumbsVOS.add(crumb);
                });
            } else {
                log.warn("ESSearch调用gulimall-product/brand/info/batch失败");
            }
        }

        /**
         * 构建面包屑导航，三级分类部分
         */

        /**
         * 构建面包屑导航--规格参数部分
         * 从请求参数规格参数部分，
         * 请求参数中有规格参数部分条件，才构建
         // &attrs=1_陶瓷:铝合金&attrs=2_anzhuo:apple
         */
        List<String> queryAttrs = param.getAttrs();
        if (!CollectionUtils.isEmpty(queryAttrs)) {
            List<SearchResult.BreadCrumbsVO> crumbsVOS = queryAttrs.stream().map(attrStr -> {
                // id_value
                String[] attrInfo = attrStr.split("_");
                SearchResult.BreadCrumbsVO breadCrumbsVO = new SearchResult.BreadCrumbsVO();
                breadCrumbsVO.setAttrValue(attrInfo[1]);
                // 请求参数中去掉当前属性之后的链接地址
                String link = "";
                try {
                    // request对路径进行了编码。我们得先把自己的参数编码。才能在路径正正确匹配并替换
                    String encode = URLEncoder.encode(attrStr, "utf-8");
                    // java编码后，空格会被替换为 + ，而浏览器会编码为 %20；英文()会被编码成%28,%29，而浏览器不会编码英文()
                    // 所以我们还得把+替换为浏览器的规则
                    encode = encode.replace("+", "%20").replace("%28", "(").replace("%29", ")");
                    // 去掉 &attrs=1_陶瓷
                    URLDecoder.decode(param.getQueryString(), "iso-8859-1");
                    link = param.getQueryString().replace("&attrs=" + encode, "").replace("attrs=" + encode, "");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                breadCrumbsVO.setLink("http://search.gulimall.com/list.html?" + link);
                // 保存请求参数中的attrId
                result.getParamAttrIds().add(Long.parseLong(attrInfo[0]));
                // 远程调用
                try {
                    R r = productFeignService.info(Long.valueOf(attrInfo[0]));
                    if (r.getCode() == 0) {
                        AttrRespVO attrRespVO = r.getData("attr", AttrRespVO.class);
                        breadCrumbsVO.setAttrName(attrRespVO.getAttrName());
                    }
                } catch (Exception e) {
                    log.error("gulimall-search调用gulimall-product根据attrId查询attrInfo失败：{}", e);
                }
                return breadCrumbsVO;
            }).collect(Collectors.toList());
            breadCrumbsVOS.addAll(crumbsVOS);

        }
        // 保存所有面包屑
        result.setBreadCrumbsNavs(breadCrumbsVOS);
        // 返回结果
        return result;
    }
}
