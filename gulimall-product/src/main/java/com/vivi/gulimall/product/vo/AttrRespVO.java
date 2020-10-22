package com.vivi.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/15 22:36
 *
 * 对数据库查询出的数据组合封装后要返回给前端的页面对象
 *
 * "catelogName": "手机/数码/手机", //所属分类名字
 * "groupName": "主体", //所属分组名字
 */
@Data
public class AttrRespVO {

    private static final long serialVersionUID = 1L;

    /**
     * 属性id
     */
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;
    /**
     * 是否需要检索[0-不需要，1-需要]
     */
    private Integer searchType;
    /**
     * 属性图标
     */
    private String icon;
    /**
     * 可选值列表[用分号分隔]
     */
    private String valueSelect;
    /**
     * 值类型[0-为单个值，1-可以选择多个值]
     */
    private Integer valueType;
    /**
     * 属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]
     */
    private Integer attrType;
    /**
     * 启用状态[0 - 禁用，1 - 启用]
     */
    private Long enable;
    /**
     * 所属分类
     */
    private Long catelogId;
    /**
     * 快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整
     */
    private Integer showDesc;

    /**
     * 所属三级分类(菜单)的名字
     */
    private String catelogName;

    /**
     * 所属属性分组的id
     */
    private Long attrGroupId;

    /**
     * 所属属性组的名字
     */
    private String attrGroupName;

    /**
     * 所属分类的完整层级
     */
    private List<Long> catelogPath;
}
