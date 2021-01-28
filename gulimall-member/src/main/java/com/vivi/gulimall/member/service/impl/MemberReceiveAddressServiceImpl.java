package com.vivi.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.to.MemberAddressTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.member.dao.MemberReceiveAddressDao;
import com.vivi.gulimall.member.entity.MemberReceiveAddressEntity;
import com.vivi.gulimall.member.service.MemberReceiveAddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<MemberAddressTO> listByMemberId(Long memberId) {

        List<MemberReceiveAddressEntity> addressEntities = this.list(new QueryWrapper<MemberReceiveAddressEntity>().eq("member_id", memberId));
        return addressEntities.stream().map(entity -> convertAddressEntity2AddressTO(entity)).collect(Collectors.toList());
    }

    private  MemberAddressTO convertAddressEntity2AddressTO(MemberReceiveAddressEntity entity) {
        MemberAddressTO memberAddressTO = new MemberAddressTO();
        BeanUtils.copyProperties(entity, memberAddressTO);
        return memberAddressTO;
    }

}