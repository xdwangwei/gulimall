package com.vivi.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangwei
 * 2020/10/22 22:35
 */
@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.host-addr}")
    private String esHost;

    @Value("${elasticsearch.port}")
    private Integer port;
    @Value("${elasticsearch.schema}")
    private String schema;


    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, port, schema)));

        return client;
    }
}
