package com.vivi.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.vivi.common.to.MemberInfoTO;
import com.vivi.common.to.MemberLoginTO;
import com.vivi.common.to.MemberRegisterTO;
import com.vivi.common.to.WeiboUserAuthTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vivi.gulimall.member.entity.MemberEntity;
import com.vivi.gulimall.member.service.MemberService;


/**
 * 会员
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:51:13
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);
        return R.ok();
    }

    /**
     * 新用户注册
     * @param registerTO
     * @return
     */
    @RequestMapping("/register")
    public R register(@RequestBody MemberRegisterTO registerTO){
        memberService.register(registerTO);
        return R.ok();
    }

    /**
     * 用户登录
     */
    @RequestMapping("/login")
    public R login(@RequestBody MemberLoginTO loginTO){
        // 登录失败会由异常处理机制处理
        MemberInfoTO infoTO = memberService.login(loginTO);
        return R.ok().setData(infoTO);
    }

    /**
     * 社交登录--微博
     */
    @RequestMapping("/weibo/login")
    public R weiboLogin(@RequestBody WeiboUserAuthTO authTO){
        // 登录失败会由异常处理机制处理
        MemberInfoTO infoTO = memberService.weiboLogin(authTO);
        return R.ok().setData(infoTO);
    }



    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
