package com.vivi.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.constant.MemberConstant;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.to.MemberInfoTO;
import com.vivi.common.to.MemberLoginTO;
import com.vivi.common.to.MemberRegisterTO;
import com.vivi.common.to.WeiboUserAuthTO;
import com.vivi.common.utils.HttpUtils;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.member.dao.MemberDao;
import com.vivi.gulimall.member.entity.MemberEntity;
import com.vivi.gulimall.member.entity.MemberLevelEntity;
import com.vivi.gulimall.member.service.MemberLevelService;
import com.vivi.gulimall.member.service.MemberService;
import com.vivi.gulimall.member.vo.WeiboUserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean register(MemberRegisterTO registerTO) {

        MemberEntity memberEntity = new MemberEntity();

        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        // 默认等级
        memberEntity.setLevelId(levelEntity.getId());
        // 注册来源
        memberEntity.setRegisterType(MemberConstant.REGISTER_TYPE_GULIMALL);
        // 创建时间
        memberEntity.setCreateTime(new Date());
        // 加密密码
        memberEntity.setPassword(passwordEncoder.encode(registerTO.getPassword()));
        // 用户名
        memberEntity.setUsername(registerTO.getUsername());
        memberEntity.setNickname(registerTO.getUsername());
        // 手机号
        memberEntity.setMobile(registerTO.getPhone());

        try {
            this.save(memberEntity);
            // 唯一索引报错
        } catch (DuplicateKeyException e) {
            throw new BizException(BizCodeEnum.MEMBER_ALREADY_EXIST, "用户名或手机号已存在");
        }

        return true;
    }

    @Override
    public MemberInfoTO login(MemberLoginTO loginTO) {
        // 查询账户是否存在
        MemberEntity entity = this.baseMapper.getByAccount(loginTO.getAccount());
        if (entity == null) {
            throw new BizException(BizCodeEnum.MEMBER_NOT_EXIST);
        }
        // 存在则比对密码，比对密码失败
        if (!passwordEncoder.matches(loginTO.getPassword(), entity.getPassword())) {
            throw new BizException(BizCodeEnum.MEMBER_ACCOUNT_PASSWORD_NOT_MATCH);
        }
        // 认证成功
        return convertMemberEntity2MemberInfoTO(entity);
    }

    /**
     * 如果用户是第一次使用微博登录，则创建一个member与之关联，否则，更新其本次登录用的token的过期时间即可
     * @param authTO
     * @return
     */
    @Transactional
    @Override
    public MemberInfoTO weiboLogin(WeiboUserAuthTO authTO) {

        // 判断该用户是否为第一次登录
        String uid = authTO.getUid();
        MemberEntity entity = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        // 第一次登录，创建新用户
        if (entity == null) {
            MemberEntity memberEntity = new MemberEntity();
            // 默认等级
            MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
            memberEntity.setLevelId(levelEntity.getId());
            // 注册来源
            memberEntity.setRegisterType(MemberConstant.REGISTER_TYPE_WEIBO);
            memberEntity.setAccessToken(authTO.getAccessToken());
            memberEntity.setExpireIn(authTO.getExpiresIn());
            memberEntity.setSocialUid(uid);
            // 创建时间
            memberEntity.setCreateTime(new Date());

            // 通过微博accessToken拿到该用户在微博平台的基本信息，用于注册
            WeiboUserInfoVO userFromWeibo = getUserFromWeibo(authTO.getAccessToken(), uid);
            if (userFromWeibo != null) {
                // 用户名
                if (StringUtils.isNotBlank(userFromWeibo.getScreen_name())) {
                    memberEntity.setUsername(userFromWeibo.getScreen_name());
                }
                // 昵称
                if (StringUtils.isNotBlank(userFromWeibo.getName())) {
                    memberEntity.setNickname(userFromWeibo.getName());
                }
                // 性别
                if (StringUtils.isNotBlank(userFromWeibo.getGender())) {
                    memberEntity.setGender(userFromWeibo.getName().equals('m') ? 0 : 1);
                }
                // 头像
                if (StringUtils.isNotBlank(userFromWeibo.getAvatar_hd())) {
                    memberEntity.setHeader(userFromWeibo.getAvatar_hd());
                }
                // 城市
                if (StringUtils.isNotBlank(userFromWeibo.getLocation())) {
                    memberEntity.setCity(userFromWeibo.getLocation());
                }
            }

            // 保存
            this.save(memberEntity);

            // 返回
            return convertMemberEntity2MemberInfoTO(memberEntity);
        } else {
            // 否则，更新其本次登录用的token和过期时间即可
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setId(entity.getId());
            memberEntity.setAccessToken(authTO.getAccessToken());
            memberEntity.setExpireIn(authTO.getExpiresIn());
            this.updateById(memberEntity);
            // 上面这些信息不用返回给前端，前端只需要拿到基本信息即可
            return convertMemberEntity2MemberInfoTO(entity);
        }
    }

    /**
     * 将从数据库中查出来的用户信息转换成前端需要的用户信息
     * @param entity
     * @return
     */
    private MemberInfoTO convertMemberEntity2MemberInfoTO(MemberEntity entity) {
        MemberInfoTO memberInfoTO = new MemberInfoTO();
        // 基本属性拷贝
        BeanUtils.copyProperties(entity, memberInfoTO);
        // 设置会员等级名
        MemberLevelEntity levelEntity = memberLevelService.getById(entity.getLevelId());
        memberInfoTO.setLevel(levelEntity.getName());
        return memberInfoTO;
    }

    /**
     * // 通过微博accessToken拿到该用户在微博平台的基本信息，用于注册
     */
    private WeiboUserInfoVO getUserFromWeibo(String accessToken, String uid) {
        MemberEntity entity = new MemberEntity();
        HashMap<String, String> param = new HashMap<>();
        param.put("access_token", accessToken);
        param.put("uid", uid);
        try {
            HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), param);
            if (response.getStatusLine().getStatusCode() == 200) {
                String json = EntityUtils.toString(response.getEntity());
                WeiboUserInfoVO weiboUserInfoVO = JSON.parseObject(json, WeiboUserInfoVO.class);
                return weiboUserInfoVO;
            } else {
                log.warn("获取用户微博信息失败：{}", EntityUtils.toString(response.getEntity()));
                return null;
            }
        } catch (Exception e) {
            log.warn("获取用户微博信息失败：{}", e.getMessage());
            return null;
        }
    }
}