package com.vivi.gulimall.search.vo;

import com.vivi.common.to.SkuESModel;
import lombok.Data;
import org.w3c.dom.Attr;

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
    private Integer totalPage;

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
    private Integer totalCount;


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
}
