package com.vivi.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 采购信息
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:47:27
 */
@Data
@TableName("wms_purchase")
public class PurchaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private Long assigneeId;
	/**
	 * 
	 */
	private String assigneeName;
	/**
	 * 
	 */
	private String phone;
	/**
	 * 
	 */
	private Integer priority;
	/**
	 * 
	 */
	private Integer status;
	/**
	 * 
	 */
	private Long wareId;
	/**
	 * 
	 */
	private BigDecimal amount;
	/**
	 * 
	 */
	private Date createTime;
	/**
	 * 
	 */
	private Date updateTime;

}
