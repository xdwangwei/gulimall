package com.vivi.gulimall.product.web;

import com.vivi.gulimall.product.entity.CategoryEntity;
import com.vivi.gulimall.product.service.CategoryService;
import com.vivi.gulimall.product.vo.Catelog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.PipedReader;
import java.util.List;
import java.util.Map;

/**
 * @author wangwei
 * 2020/10/24 12:56
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @RequestMapping({"/", "/index"})
    public String index(Model model) {
        List<CategoryEntity> categories = categoryService.getLevel1Categories();
        model.addAttribute("categories", categories);
        return "index";
    }

    /**
     * 首页三级分类渲染所需要的的数据模型
     * @return
     */
    @RequestMapping("/catelog.json")
    @ResponseBody
    public Map<String, List<Catelog2VO>> getCatelogJson() {
        return categoryService.getCatelogJson();
    }
}
