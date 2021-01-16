package com.vivi.gulimall.thirdparty.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.vivi.common.utils.R;
import com.vivi.gulimall.thirdparty.service.OSSService;
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
    OSSService ossService;

    @RequestMapping("/upload/policy")
    public R policy() {
        return R.ok().put("data", ossService.getUploadPolicy());
    }

}
