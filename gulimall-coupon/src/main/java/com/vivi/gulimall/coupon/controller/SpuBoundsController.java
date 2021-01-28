package com.vivi.gulimall.coupon.controller;

import com.vivi.common.to.SpuBoundsTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.R;
import com.vivi.gulimall.coupon.entity.SpuBoundsEntity;
import com.vivi.gulimall.coupon.service.SpuBoundsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 商品spu积分设置
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:52:21
 */
@RestController
@RequestMapping("coupon/spubounds")
public class SpuBoundsController {

    @Autowired
    private SpuBoundsService spuBoundsService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("coupon:spubounds:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuBoundsService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("coupon:spubounds:info")
    public R info(@PathVariable("id") Long id){
		SpuBoundsEntity spuBounds = spuBoundsService.getById(id);

        return R.ok().put("spuBounds", spuBounds);
    }

    /**
     * 远程调用，保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("coupon:spubounds:save")
    public R saveSpuBounds(@RequestBody SpuBoundsEntity spuBounds){
		spuBoundsService.saveSpuBoundS(spuBounds);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("coupon:spubounds:update")
    public R update(@RequestBody SpuBoundsEntity spuBounds){
		spuBoundsService.updateById(spuBounds);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("coupon:spubounds:delete")
    public R delete(@RequestBody Long[] ids){
		spuBoundsService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @RequestMapping("/info/spuId/{spuId}")
    // @RequiresPermissions("coupon:spubounds:info")
    public R getBySpuId(@PathVariable("spuId") Long spuId){
        SpuBoundsTO spuBounds = spuBoundsService.getBySpuId(spuId);

        return R.ok().setData(spuBounds);
    }

}
