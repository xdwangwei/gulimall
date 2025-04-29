package com.vivi.gulimall.search.service;

import java.io.IOException;
import java.util.List;

import com.vivi.common.to.SkuESModel;

/**
 * @author wangwei
 * 2020/10/22 23:17
 */
public interface ProductESService {
    boolean batchSave(List<SkuESModel> list) throws IOException;
}
