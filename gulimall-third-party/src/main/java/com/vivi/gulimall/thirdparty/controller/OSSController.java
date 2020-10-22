package com.vivi.gulimall.thirdparty.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.vivi.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author wangwei
 * 2020/10/10 11:09
 */
@RestController
@RequestMapping("/thirdparty/oss")
public class OSSController {

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


    @RequestMapping("/upload/policy")
    public R policy() {
        // https://vivi-gulimall.oss-cn-hangzhou.aliyuncs.com
        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        // String callbackUrl = "http://88.88.88.88:8888";
        // 每一天产生一个文件夹
        String dir = LocalDate.now().toString() + "/"; // 用户上传文件时指定的前缀。

        Map<String, String> respMap = null;
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

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));
        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return R.ok().put("data", respMap);
    }

    // public static void main(String[] args) {
    //     LocalDate now = LocalDate.now();
    //     System.out.println(now);
    //     System.out.println(now.toString());
    // }
}
