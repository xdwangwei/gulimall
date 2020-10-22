package com.vivi.gulimall.search.service.impl;

import com.vivi.gulimall.search.service.ProductESService;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangwei
 * 2020/10/22 23:17
 */
@Service
public class ProductESServiceImpl implements ProductESService {

    @Autowired
    private RestHighLevelClient esClient;
}
