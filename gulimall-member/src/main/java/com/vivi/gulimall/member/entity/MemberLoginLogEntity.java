package com.vivi.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会员登录记录
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:51:13
 */
@Data
@TableName("ums_member_login_log")
public class MemberLoginLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * member_id
	 */
	private Long memberId;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * ip
	 */
	private String ip;
	/**
	 * city
	 */
	private String city;
	/**
	 * 登录类型[1-web，2-app]
	 */
	private Integer loginType;

}
