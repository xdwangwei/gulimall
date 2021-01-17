package com.vivi.gulimall.auth.service.impl;

import com.vivi.common.to.MemberInfoTO;
import com.vivi.common.to.MemberLoginTO;
import com.vivi.common.utils.R;
import com.vivi.common.vo.MemberInfoVO;
import com.vivi.gulimall.auth.exception.LoginPageException;
import com.vivi.gulimall.auth.feign.MemberFeignService;
import com.vivi.gulimall.auth.service.LoginService;
import com.vivi.gulimall.auth.vo.LoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangwei
 * 2021/1/14 20:27
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public MemberInfoVO doLogin(LoginVO loginVO) {
        MemberLoginTO memberLoginTO = new MemberLoginTO();
        BeanUtils.copyProperties(loginVO, memberLoginTO);
        R r = memberFeignService.login(memberLoginTO);
        // 登录失败
        if (r.getCode() != 0) {
            throw new LoginPageException(r.getCode(), r.getData("msg", String.class));
        }
        return convertMemberInfoTO2MemberInfoVO(r.getData(MemberInfoTO.class));
    }

    private MemberInfoVO convertMemberInfoTO2MemberInfoVO(MemberInfoTO infoTO) {
        MemberInfoVO infoVO = new MemberInfoVO();
        BeanUtils.copyProperties(infoTO, infoVO);
        return infoVO;
    }
}
