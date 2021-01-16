package com.vivi.gulimall.thirdparty.service;

import com.vivi.common.utils.R;

import java.util.Map;

/**
 * @author wangwei
 * 2021/1/13 15:13
 */
public interface OSSService {

    /**
     * 返回服务端的签名信息
     * @return
     */
    Map<String, String> getUploadPolicy();
}
