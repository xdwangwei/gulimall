package com.vivi.gulimall.coupon.service.impl;

import com.vivi.common.to.SkuDiscountTO;
import com.vivi.gulimall.coupon.entity.MemberPriceEntity;
import com.vivi.gulimall.coupon.entity.SkuFullReductionEntity;
import com.vivi.gulimall.coupon.entity.SkuLadderEntity;
import com.vivi.gulimall.coupon.service.MemberPriceService;
import com.vivi.gulimall.coupon.service.SkuDiscountService;
import com.vivi.gulimall.coupon.service.SkuFullReductionService;
import com.vivi.gulimall.coupon.service.SkuLadderService;
import org.apache.tomcat.jni.BIOCallback;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.Email;
import javax.xml.ws.ServiceMode;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangwei
 * 2020/10/19 16:29
 */
@Service
public class SkuDiscountServiceImpl implements SkuDiscountService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private SkuFullReductionService skuFullReductionService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Transactional
    @Override
    public boolean save(SkuDiscountTO skuDiscountTO) {
        // 处理打折，存在打折记录
        if (skuDiscountTO.getFullCount() > 0 && skuDiscountTO.getDiscount().compareTo(BigDecimal.ZERO) == 1) {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            BeanUtils.copyProperties(skuDiscountTO, skuLadderEntity);
            skuLadderEntity.setAddOther(skuDiscountTO.getCountStatus());
            skuLadderService.save(skuLadderEntity);
        }
        // 处理满减，存在满减记录
        if (skuDiscountTO.getFullPrice().compareTo(BigDecimal.ZERO) == 1
                && skuDiscountTO.getReducePrice().compareTo(BigDecimal.ZERO) == 1) {
            SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(skuDiscountTO, skuFullReductionEntity);
            skuFullReductionEntity.setAddOther(skuDiscountTO.getPriceStatus());
            skuFullReductionService.save(skuFullReductionEntity);
        }
        // 处理会员价格
        List<SkuDiscountTO.MemberPrice> memberPriceList = skuDiscountTO.getMemberPrice();
        if (!CollectionUtils.isEmpty(memberPriceList)) {
            List<MemberPriceEntity> memberPriceEntityList = memberPriceList.stream().filter(memberPrice -> memberPrice.getPrice().compareTo(BigDecimal.ZERO) == 1)
                    .map(memberPrice -> {
                        MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                        memberPriceEntity.setSkuId(skuDiscountTO.getSkuId());
                        memberPriceEntity.setAddOther(1);
                        memberPriceEntity.setMemberLevelId(memberPrice.getId());
                        memberPriceEntity.setMemberPrice(memberPrice.getPrice());
                        memberPriceEntity.setMemberLevelName(memberPrice.getName());
                        return memberPriceEntity;
                    }).collect(Collectors.toList());
            memberPriceService.saveBatch(memberPriceEntityList);
        }
        return true;
    }
}
