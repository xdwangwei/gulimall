package com.vivi.gulimall.member.vo;

import lombok.Data;


/**
 * @author wangwei
 * 2021/1/15 16:29
 *
 * 根据accessToken得到的用户微博平台基本信息
 *
 * 注释了的字段都是微博文档中未说明，但返回结果中有的其他字段
 * https://open.weibo.com/wiki/2/users/show
 */
@Data
public class WeiboUserInfoVO {

    /**
     * {
     *     "id": 7354324591,
     *     "idstr": "7354324591",
     *     "class": 1,
     *     "screen_name": "余生余生愿你安好",
     *     "name": "余生余生愿你安好",
     *     "province": "11",
     *     "city": "1",
     *     "location": "北京 东城区",
     *     "description": "",
     *     "url": "https://wangwi.site",
     *     "profile_image_url": "https://tvax1.sinaimg.cn/crop.0.0.996.996.50/0081HZMzly8gdm6eh9kx0j30ro0rogmf.jpg?KID=imgbed,tva&Expires=1610722711&ssig=CZccf17tMW",
     *     "cover_image_phone": "http://ww1.sinaimg.cn/crop.0.0.640.640.640/549d0121tw1egm1kjly3jj20hs0hsq4f.jpg",
     *     "profile_url": "u/7354324591",
     *     "domain": "",
     *     "weihao": "",
     *     "gender": "m",
     *     "followers_count": 2,
     *     "friends_count": 9,
     *     "pagefriends_count": 0,
     *     "statuses_count": 0,
     *     "video_status_count": 0,
     *     "video_play_count": 0,
     *     "favourites_count": 0,
     *     "created_at": "Fri Dec 13 14:55:39 +0800 2019",
     *     "following": false,
     *     "allow_all_act_msg": false,
     *     "geo_enabled": true,
     *     "verified": false,
     *     "verified_type": -1,
     *     "remark": "",
     *     "insecurity": {
     *         "sexual_content": false
     *     },
     *     "ptype": 0,
     *     "allow_all_comment": true,
     *     "avatar_large": "https://tvax1.sinaimg.cn/crop.0.0.996.996.180/0081HZMzly8gdm6eh9kx0j30ro0rogmf.jpg?KID=imgbed,tva&Expires=1610722711&ssig=EErXfclHmj",
     *     "avatar_hd": "https://tvax1.sinaimg.cn/crop.0.0.996.996.1024/0081HZMzly8gdm6eh9kx0j30ro0rogmf.jpg?KID=imgbed,tva&Expires=1610722711&ssig=sfZ9VL1k8y",
     *     "verified_reason": "",
     *     "verified_trade": "",
     *     "verified_reason_url": "",
     *     "verified_source": "",
     *     "verified_source_url": "",
     *     "follow_me": false,
     *     "like": false,
     *     "like_me": false,
     *     "online_status": 0,
     *     "bi_followers_count": 0,
     *     "lang": "zh-cn",
     *     "star": 0,
     *     "mbtype": 0,
     *     "mbrank": 0,
     *     "block_word": 0,
     *     "block_app": 0,
     *     "credit_score": 80,
     *     "user_ability": 0,
     *     "urank": 0,
     *     "story_read_state": -1,
     *     "vclub_member": 0,
     *     "is_teenager": 0,
     *     "is_guardian": 0,
     *     "is_teenager_list": 0,
     *     "pc_new": 0,
     *     "special_follow": false,
     *     "planet_video": 0,
     *     "video_mark": 0,
     *     "live_status": 0
     * }
     */

    /**
     * uid
     */
    private long id;
    /**
     * 字符串形式的uid
     */
    private String idstr;

    // private String class;

    /**
     * 用户昵称
     */
    private String screen_name;
    /**
     * 友好显示昵称
     */
    private String name;
    /**
     * 所在省级Id
     */
    private String province;
    /**
     * 所在城市id
     */
    private String city;
    /**
     * 所在地 '北京 东城区'
     */
    private String location;
    /**
     * 用户个人描述
     */
    private String description;
    /**
     * 用户博客地址
     */
    private String url;
    /**
     * 用户头像地址，50x50像素
     */
    private String profile_image_url;
    /**
     * 用户的微博统一URL地址
     */
    private String profile_url;
    /**
     * 用户的个性化域名
     */
    private String domain;
    /**
     * 用户的微号
     */
    private String weihao;
    /**
     * 性别，m：男、f：女、n：未知
     */
    private String gender;
    /**
     * 	粉丝数
     */
    private int followers_count;
    /**
     * 关注数
     */
    private int friends_count;
    /**
     * 微博数
     */
    private int statuses_count;

    // private String video_status_count;
    //
    // private String video_play_count;
    /**
     * 收藏数
     */
    private int favourites_count;
    /**
     * 用户创建（注册）时间
     */
    private String created_at;
    /**
     * 暂未支持
     */
    private boolean following;
    /**
     * 是否允许所有人给我发私信，true：是，false：否
     */
    private boolean allow_all_act_msg;
    /**
     * 是否允许标识用户的地理位置，true：是，false：否
     */
    private boolean geo_enabled;
    /**
     * 是否是微博认证用户，即加V用户，true：是，false：否
     */
    private boolean verified;
    /**
     * 暂未支持
     */
    private int verified_type;
    /**
     * 用户备注信息，只有在查询用户关系时才返回此字段
     */
    private String remark;
    // "insecurity": {
    //     "sexual_content": false
    // },
    private String ptype;
    /**
     * 是否允许所有人对我的微博进行评论，true：是，false：否
     */
    private boolean allow_all_comment;
    /**
     * 用户头像地址（大图），180×180像素
     */
    private String avatar_large;
    /**
     * 用户头像地址（高清），高清头像原图
     */
    private String avatar_hd;
    /**
     * 认证原因
     */
    private String verified_reason;
    // private String verified_trade;
    // private String verified_reason_url;
    // private String verified_source;
    // private String verified_source_url;
    /**
     * 该用户是否关注当前登录用户，true：是，false：否
     */
    private boolean follow_me;
    // private boolean like;
    // private boolean like_me;
    /**
     * 用户的在线状态，0：不在线、1：在线
     */
    private int online_status;
    /**
     * 用户的互粉数
     */
    private int bi_followers_count;
    /**
     * 用户当前的语言版本，zh-cn：简体中文，zh-tw：繁体中文，en：英语
     */
    private String lang;


    // private int star;
    // private int mbtype;
    // private int mbrank;
    // private int block_word;
    // private int block_app;
    // private int credit_score;
    // private int user_ability;
    // private int urank;
    // private int story_read_state;
    // private int vclub_member;
    // private int is_teenager;
    // private int is_guardian;
    // private int is_teenager_list;
    // private int pc_new;
    // private boolean special_follow;
    // private int planet_video;
    // private int video_mark;
    // private int live_status;
}
