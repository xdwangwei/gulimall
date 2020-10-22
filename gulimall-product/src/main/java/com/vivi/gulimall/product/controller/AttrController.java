package com.vivi.gulimall.product.controller;

import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.R;
import com.vivi.gulimall.product.entity.AttrEntity;
import com.vivi.gulimall.product.entity.ProductAttrValueEntity;
import com.vivi.gulimall.product.service.AttrService;
import com.vivi.gulimall.product.vo.AttrRespVO;
import com.vivi.gulimall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品属性
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:46
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 指定分类下的基本属性
     * @param params
     * @return
     */
    @RequestMapping("/{type}/list/{catelogId}")
    // @RequiresPermissions("product:attr:list")
    public R baseList(@RequestParam Map<String, Object> params,
                      @PathVariable("type") String type,
                      @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrService.queryPage(params, type, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		// AttrEntity attr = attrService.getById(attrId);
        AttrRespVO attr = attrService.getDetailById(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody /*AttrEntity attr*/ AttrVO attrVO){
        // save()只能保存attr信息
		// attrService.save(attr);
        // 前端传来的attrVO中不仅包含attr信息，还包含所属分组等信息
		attrService.saveDetail(attrVO);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody /*AttrEntity attr*/AttrVO attrVO){
		// attrService.updateById(attr);
        attrService.updateCascadeById(attrVO);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		// attrService.removeByIds(Arrays.asList(attrIds));
        attrService.removeCascadeByIds(Arrays.asList(attrIds));
        return R.ok();
    }

}
