package com.vivi.gulimall.product.controller;

import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.R;
import com.vivi.gulimall.product.entity.CategoryEntity;
import com.vivi.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 商品三级分类
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:category:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 全部分类信息，以树形结构展示
     */
    @RequestMapping("/list/tree")
    // @RequiresPermissions("product:category:list")
    public R listWithTree(){
        return R.ok().put("list", categoryService.listWithTree());
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    // @RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		// categoryService.updateById(category);
		categoryService.updateCascadeById(category);

        return R.ok();
    }

    /**
     * 删除
     * @RequestBody,前端必须发送post请求
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
        // removeByIds是自己生成的删除方法，只执行删除功能
		// categoryService.removeByIds(Arrays.asList(catIds));

		// 实际上删除菜单前需要检查当前菜单是否在别处被引入，不能轻易删除，即便是逻辑删除也不行
        // 所以自己写一个包含这些逻辑处理的方法
        categoryService.removeBatchByIds(Arrays.asList(catIds));

        return R.ok();
    }

    public static void main(String[] args) {
        System.out.println(Long.MAX_VALUE);
    }

}
