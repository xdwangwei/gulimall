package com.vivi.gulimall.search.vo;

import com.vivi.common.to.SkuESModel;
import lombok.Data;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangwei
 * 2020/10/28 16:40
 *
 * 检索结果组合
 */
@Data
public class SearchResult {

    /**
     * 查询到的所有商品
     */
    private List<SkuESModel> SkuList;

    /**
     * 总共几页
     */
    private Long totalPage;

    /**
     * 当前页码
     */
    private Integer currPage;

    /**
     * 每页显示几个
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long totalCount;

    /**
     * 页码导航
     */
    private List<Integer> pageNavs;


    /*=====================聚合分析=====================*/
    /**
     * 查询到的所有商品所涉及的所有品牌
     */
    private List<BrandVO> brands;

    /**
     * 查询到的所有商品所涉及的所有分类
     */
    private List<CatelogVO> catelogs;

    /**
     * 查询到的所有商品所涉及的所有属性(规格)
     */
    private List<AttrVO> attrs;

    /**
     * 请求参数中已携带的attr的id集合，当前请求的搜索结果页不再附加这些attr进行搜索
     */
    private List<Long> paramAttrIds = new ArrayList<>();


    //=====================面包屑导航================================
    // 就是把规格属性查询参数一个一个显示出来，点击 × 号，能够自动刷新出不带这个参数时候的结果

    private List<BreadCrumbsVO> breadCrumbsNavs;

    @Data
    public static class BrandVO {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatelogVO {
        private Long catelogId;
        private String catelogName;
    }

    @Data
    public static class AttrVO {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class BreadCrumbsVO {
        private String attrName;

        private String attrValue;
        // 当前属性点x后，跳到哪
        private String link;
    }
}
