package com.vivi.common.constant;

import org.springframework.util.StringUtils;

import javax.xml.transform.sax.SAXTransformerFactory;

/**
 * @author wangwei
 * 2020/10/16 19:58
 */
public class ProductConstant {

    public enum AttrTypeEnum {
        ATTR_TYPE_BASE(1, "base", "基本属性"),
        ATTR_TYPE_SALE(0, "sale", "销售属性");

        private int value;
        private String name;
        private String desc;

        AttrTypeEnum(int value, String name, String desc) {
            this.value = value;
            this.name = name;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getDesc() { return desc; }
    }

    /**
     * spu状态
     */
    public enum SpuPublishStatus {
        CREATED(0, "刚创建"),
        UP(1,  "已上架"),
        DOWN(2,  "已下架");

        private int value;
        private String desc;

        SpuPublishStatus(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }


        public String getDesc() { return desc; }
    }

    /**
     * gulimall-product操作redis的键
     */
    public static class RedisKey {
        public static final String CATELOG_JSON_VALUE = "catelogJson";

        public static final String CATELOG_JSON_LOCK = "catelogJsonLock";
    }

    /**
     * gulimall-product使用springCache时，每个chache的名字(缓存分区)
     */
    public static class CacheName {
        public static final String PRODUCT_CATEGORY = "product-category";
    }

}
