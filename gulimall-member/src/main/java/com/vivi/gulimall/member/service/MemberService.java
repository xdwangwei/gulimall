package com.vivi.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.to.MemberInfoTO;
import com.vivi.common.to.MemberLoginTO;
import com.vivi.common.to.MemberRegisterTO;
import com.vivi.common.to.WeiboUserAuthTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:51:13
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 新用户注册
     * @param registerTO
     * @return
     */
    boolean register(MemberRegisterTO registerTO);

    /**
     * 账号密码登录
     * @param loginTO
     * @return
     */
    MemberInfoTO login(MemberLoginTO loginTO);

    /**
     * 使用微博的访问令牌获取用户信息完成登录
     * @param authTO
     * @return
     */
    MemberInfoTO weiboLogin(WeiboUserAuthTO authTO);

}

