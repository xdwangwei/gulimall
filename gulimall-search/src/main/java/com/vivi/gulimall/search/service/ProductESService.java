package com.vivi.gulimall.search.service;

import com.sun.el.parser.BooleanNode;
import com.vivi.common.to.SkuESModel;

import java.io.IOException;
import java.util.List;

/**
 * @author wangwei
 * 2020/10/22 23:17
 */
public interface ProductESService {
    boolean batchSave(List<SkuESModel> list) throws IOException;
}
