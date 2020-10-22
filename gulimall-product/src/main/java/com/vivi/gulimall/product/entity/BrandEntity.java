package com.vivi.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.vivi.common.valid.ListValue;
import com.vivi.gulimall.product.valid.AddBrandValidateGroup;
import com.vivi.gulimall.product.valid.UpdateBrandStatusValidateGroup;
import com.vivi.gulimall.product.valid.UpdateBrandValidateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 *
 * @NotNull: CharSequence, Collection, Map 和 Array 对象不能是 null, 但可以是空集（size = 0）。
 * @NotEmpty: CharSequence, Collection, Map 和 Array 对象不能是 null 并且相关对象的 size 大于 0。
 * @NotBlank: String 不是 null 且 至少包含一个字符
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 *
	 * 新增品牌，不能指定id，数据库自增
	 * 修改品牌信息，必须指定id
	 */
	@TableId
	@Null(groups = {AddBrandValidateGroup.class}, message = "新增品牌时不能传入id")
	@NotNull(groups = {UpdateBrandValidateGroup.class, UpdateBrandStatusValidateGroup.class}, message = "修改某个品牌，必须传入id")
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class}, message = "品牌名不能为空")
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "品牌logo地址必须是一个合法的url", groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class})
	@NotBlank(message = "新增品牌必须传入品牌logo地址", groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "新增品牌必须传入品牌状态值", groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class, UpdateBrandStatusValidateGroup.class})
	// 使用自定义注解，也可使用正则表达式@Pattern()
	@ListValue(value = {0, 1}, groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class, UpdateBrandStatusValidateGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "新增品牌必须传入品牌检索首字母", groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "品牌名必须是a-z或A-Z中的单个字符", groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "新增品牌必须传入品牌排序字段", groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class})
	@Min(value = 0, message = "品牌排序字段必须大于等于0", groups = {AddBrandValidateGroup.class, UpdateBrandValidateGroup.class})
	private Integer sort;

}
