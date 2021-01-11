package com.vivi.gulimall.product.controller;

import com.vivi.common.to.BrandTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.R;
import com.vivi.gulimall.product.entity.BrandEntity;
import com.vivi.gulimall.product.service.BrandService;
import com.vivi.gulimall.product.valid.AddBrandValidateGroup;
import com.vivi.gulimall.product.valid.UpdateBrandStatusValidateGroup;
import com.vivi.gulimall.product.valid.UpdateBrandValidateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 品牌
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {


    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 树形列表
     */
    @RequestMapping("/list/tree")
    // @RequiresPermissions("product:brand:list")
    public R listTree(){
        List<BrandEntity> list = brandService.list();

        return R.ok().put("listTree", list);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/batch")
    public R getBatch(@RequestBody List<Long> ids){
        List<BrandTO> brands = brandService.getBatch(ids);
        return R.ok().setData(brands);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:brand:save")
    // 需要分组校验时就不能使用 @Valid ，要使用 @Validated
    public R save(@Validated({AddBrandValidateGroup.class}) @RequestBody BrandEntity brand){
		brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:brand:update")
    public R update(@Validated({UpdateBrandValidateGroup.class}) @RequestBody BrandEntity brand){
		// brandService.updateById(brand);
        brandService.updateCascadeById(brand);
        return R.ok();
    }

    /**
     * 只修改品牌状态信息
     * @param brand
     * @return
     */
    @RequestMapping("/update/status")
    // @RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated({UpdateBrandStatusValidateGroup.class}) @RequestBody BrandEntity brand){
        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
