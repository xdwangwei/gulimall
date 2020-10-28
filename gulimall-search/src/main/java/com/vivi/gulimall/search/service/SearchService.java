package com.vivi.gulimall.search.service;

import com.vivi.gulimall.search.vo.SearchParam;
import com.vivi.gulimall.search.vo.SearchResult;

/**
 * @author wangwei
 * 2020/10/28 16:47
 */
public interface SearchService {

    /**
     * 按照参数进行检索。并返回结果
     * @param param
     * @return
     */
    SearchResult search(SearchParam param);
}
