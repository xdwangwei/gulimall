package com.vivi.gulimall.product.controller;

import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.R;
import com.vivi.gulimall.product.entity.AttrEntity;
import com.vivi.gulimall.product.entity.AttrGroupEntity;
import com.vivi.gulimall.product.service.AttrAttrgroupRelationService;
import com.vivi.gulimall.product.service.AttrGroupService;
import com.vivi.gulimall.product.service.CategoryService;
import com.vivi.gulimall.product.vo.AttrAttrGroupRelationVO;
import com.vivi.gulimall.product.vo.AttrGroupWithAttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:47
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
        // PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 列表
     */
    @RequestMapping("/{attrGroupId}/relation/attr")
    public R relationAttr(@PathVariable("attrGroupId") Long attrGroupId){
        // PageUtils page = attrGroupService.queryPage(params);
        List<AttrEntity> list = attrGroupService.queryRelationAttr(attrGroupId);
        return R.ok().put("data", list);
    }

    /**
     * 查询当前分组可关联的属性(未被关联过的)
     * @param attrGroupId
     * @return
     */
    @RequestMapping("/{attrGroupId}/no/relation/attr")
    public R noRelationAttr(@RequestParam Map<String, Object> params,
                            @PathVariable("attrGroupId") Long attrGroupId){
        PageUtils page = attrGroupService.queryNoRelationAttr(params, attrGroupId);
        return R.ok().put("page", page);
    }

    @RequestMapping("/{catelogId}/withattr")
    public R attrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupWithAttrVO> list = attrGroupService.getAttrGroupWithAttrByCatelogId(catelogId);
        return R.ok().put("data", list);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		// 查询这个属性组所属分类的完整层级关系
        attrGroup.setCatelogPath(categoryService.findCategoryPath(attrGroup.getCatelogId()));
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		// attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
		attrGroupService.removeCascadeByIds(Arrays.asList(attrGroupIds));
        return R.ok();
    }

}
