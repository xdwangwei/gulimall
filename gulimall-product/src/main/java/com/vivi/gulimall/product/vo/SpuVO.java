package com.vivi.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author wangwei
 * 2020/10/19 11:28
 */
@Data
public class SpuVO {

    /**
     * spu基本信息
     */
    private String spuName;
    private String spuDescription;
    private Long catelogId;
    private Long brandId;
    private BigDecimal weight;
    private Integer publishStatus;

    /**
     * 描述图片集
     */
    private List<String> descript;
    /**
     * 实物图片集
     */
    private List<String> images;
    /**
     * 此spu的积分策略
     */
    private Bounds bounds;
    /**
     * 规格参数
     */
    private List<BaseAttrs> baseAttrs;
    /**
     * 此spu下的所有sku
     */
    private List<Sku> skus;

    /**
     * 积分
     */
    @Data
    public static class Bounds {

        /**
         * 购物积分
         */
        private BigDecimal buyBounds;
        /**
         * 成长积分
         */
        private BigDecimal growBounds;
    }

    /**
     * 规格参数，基本属性
     */
    @Data
    public static class BaseAttrs {

        private Long attrId;
        private String attrValues;
        /**
         * 是否快速展示
         */
        private Integer showDesc;
    }
    /**
     * 销售属性，组合形成sku
     */
    @Data
    public static class Attr {

        private Long attrId;
        private String attrName;
        private String attrValue;
    }

    /**
     * sku的图片信息
     */
    @Data
    public static class Images {

        private String imgUrl;
        /**
         * 是否是默认展示的图
         */
        private Integer defaultImg;
    }

    /**
     * 此sku的会员价
     */
    @Data
     public static class MemberPrice {

        /**
         * 会员等级表的id
         */
        private Long id;
        private String name;
        private BigDecimal price;
    }

    /**
     * sku信息
     */
    @Data
    public static class Sku {

        /**
         * sku基本信息
         */
        private String skuName;
        private BigDecimal price;
        private String skuTitle;
        private String skuSubtitle;
        /**
         * 图集
         */
        private List<Images> images;
        /**
         * 销售属性组合形成的笛卡尔积 比如 蓝色8+256g
         */
        private List<String> descar;
        /**
         * 销售属性集
         */
        private List<Attr> attr;

        /**
         * 满几件开始打折
         */
        private Integer fullCount;
        /**
         * 打几折
         */
        private BigDecimal discount;
        /**
         * 是否能与其他优惠叠加
         */
        private Integer countStatus;
        /**
         * 满多少钱可以减价
         */
        private BigDecimal fullPrice;
        /**
         * 减价多少钱
         */
        private BigDecimal reducePrice;
        /**
         * 是否能与其他优惠叠加
         */
        private Integer priceStatus;
        /**
         * 这个sku不同会员等级的价格信息
         */
        private List<MemberPrice> memberPrice;
    }
}
