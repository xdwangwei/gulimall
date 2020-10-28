package com.vivi.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.vivi.common.constant.SearchConstant;
import com.vivi.common.to.SkuESModel;
import com.vivi.gulimall.search.service.ProductESService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangwei
 * 2020/10/22 23:17
 */
@Slf4j
@Service
public class ProductESServiceImpl implements ProductESService {

    @Autowired
    private RestHighLevelClient esClient;

    @Override
    public boolean batchSave(List<SkuESModel> list) throws IOException {
        // request.add(new IndexRequest("posts").id("1")
        //         .source(XContentType.JSON,"field", "foo"));
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuESModel skuESModel : list) {
            bulkRequest.add(
                    new IndexRequest(SearchConstant.ESIndex.ES_PRODUCT_INDEX)
                    .id(skuESModel.getSkuId().toString())
                    .source(JSON.toJSONString(skuESModel), XContentType.JSON)
            );
        }
        bulkRequest.timeout(TimeValue.timeValueMinutes(2));
        BulkResponse bulk = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulk.hasFailures()) {
            List<String> strings = Arrays.stream(bulk.getItems()).map(item -> item.getId() + ", " + item.getFailure() + ", " + item.getFailureMessage() + "\n").collect(Collectors.toList());
            log.error("商品sku保存至ES失败: {}", strings);
            return false;
        }
        return true;
    }
}
