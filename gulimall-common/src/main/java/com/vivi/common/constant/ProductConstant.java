package com.vivi.common.constant;

import org.springframework.util.StringUtils;

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
}
