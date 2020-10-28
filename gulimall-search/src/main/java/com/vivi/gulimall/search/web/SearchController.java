package com.vivi.gulimall.search.web;

import com.vivi.gulimall.search.service.SearchService;
import com.vivi.gulimall.search.vo.SearchParam;
import com.vivi.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wangwei
 * 2020/10/27 16:34
 */
@Controller
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 按照检索条件进行检索。返回页面
     * @param param
     * @return
     */
    @RequestMapping("/list.html")
    public String listPage(@RequestBody SearchParam param,
                       Model model) {
        SearchResult result = searchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }
}
