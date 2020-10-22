package com.vivi.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.vivi.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/18 22:19
 */

@Data
public class AttrGroupWithAttrVO {

    private static final long serialVersionUID = 1L;

    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 关联的所有属性
     */
    private List<AttrEntity> attrs;
}
