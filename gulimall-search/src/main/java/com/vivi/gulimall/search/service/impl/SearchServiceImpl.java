package com.vivi.gulimall.search.service.impl;

import com.vivi.common.constant.ProductConstant;
import com.vivi.common.constant.SearchConstant;
import com.vivi.gulimall.search.service.SearchService;
import com.vivi.gulimall.search.vo.SearchParam;
import com.vivi.gulimall.search.vo.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author wangwei
 * 2020/10/28 16:52
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient esClient;

    /**
     * 模糊匹配keyword，
     * 过滤(分类id。品牌id，价格区间，是否有库存，规格属性)，
     * 排序，
     * 分页，
     * 高亮，
     * 聚合分析
     * @param param
     * @return
     */
    @Override
    public SearchResult search(SearchParam param) {
        // 1.模糊匹配keyword
        // 2.过滤(分类id。品牌id，价格区间，是否有库存，规格属性)，
        // 3.排序，
        // 4.分页，
        // 5.高亮，
        // 6.聚合分析
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(SearchConstant.ESIndex.ES_PRODUCT_INDEX);
        SearchResponse response = null;
        try {
            response = esClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
