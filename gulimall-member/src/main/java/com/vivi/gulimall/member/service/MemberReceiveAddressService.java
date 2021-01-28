package com.vivi.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vivi.common.to.MemberAddressTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:51:12
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<MemberAddressTO> listByMemberId(Long memberId);
}

