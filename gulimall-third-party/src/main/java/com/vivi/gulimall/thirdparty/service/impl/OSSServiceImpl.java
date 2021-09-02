package com.vivi.gulimall.thirdparty.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.gulimall.thirdparty.service.OSSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author wangwei
 * 2021/1/13 15:14
 */
@Slf4j
@Service
public class OSSServiceImpl implements OSSService {

    @Autowired
    private OSS ossClient; // 创建OSSClient实例

    @Value("${alibaba.cloud.access-key}")
    private String accessId; // 请填写您的AccessKeyId

    @Value("${alibaba.cloud.secret-key}")
    private String accessKey; // 请填写您的AccessKeySecret

    @Value("${alibaba.cloud.oss.endpoint}")
    private String endpoint; // 请填写您的 endpoint

    @Value("${alibaba.cloud.oss.bucket}")
    private String bucket; // 请填写您的 bucketname

    @Override
    public Map<String, String> getUploadPolicy() {

        Map<String, String> respMap;

        // https://vivi-gulimall.oss-cn-hangzhou.aliyuncs.com
        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        // String callbackUrl = "http://88.88.88.88:8888";
        // 每一天产生一个文件夹
        String dir = LocalDate.now().toString() + "/"; // 用户上传文件时指定的前缀。

        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));
        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            log.error("获取aliyun oss文件上传签名失败：{}", e.getMessage());
            throw new BizException(BizCodeEnum.OSS_GET_POLICY_FAILED);
        } finally {
            ossClient.shutdown();
        }
        return respMap;
    }
}
