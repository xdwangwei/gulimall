package com.vivi.gulimall.member;

import com.vivi.gulimall.member.entity.MemberEntity;
import com.vivi.gulimall.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author wangwei
 * 2020/9/12 17:30
 */
@SpringBootTest
public class GulimallMemberApplicationTests {

    @Autowired
    private MemberService memberService;

    @Test
    void contextLoads() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setMobile("13571523592");
        memberEntity.setUsername("wangwei");
        memberService.save(memberEntity);
        memberService.save(memberEntity);
    }
}
