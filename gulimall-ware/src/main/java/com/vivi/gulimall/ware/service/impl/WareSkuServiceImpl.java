package com.vivi.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.to.SkuInfoTO;
import com.vivi.common.to.SkuStockTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.common.utils.R;
import com.vivi.gulimall.ware.dao.WareSkuDao;
import com.vivi.gulimall.ware.entity.WareSkuEntity;
import com.vivi.gulimall.ware.feign.ProductFeignService;
import com.vivi.gulimall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        if (isValidId(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        String skuId = (String) params.get("skuId");
        if (isValidId(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 先判断，指定仓库指定商品，是否存在这个记录，存在则改库存，不存在则新增
     * @param wareId
     * @param skuId
     * @param num
     * @return
     */
    @Transactional
    @Override
    public boolean addStock(Long wareId, Long skuId, Integer num) {
        WareSkuEntity entity = this.getOne(new QueryWrapper<WareSkuEntity>().eq("ware_id", wareId).eq("sku_id", skuId));
        // 新增
        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        if (entity == null) {
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(num);
            wareSkuEntity.setStockLocked(0);
            try {
                R res = productFeignService.info(skuId);
                if (res.getCode() == 0) {
                    SkuInfoTO skuInfo = res.getData("skuInfo", SkuInfoTO.class);
                    wareSkuEntity.setSkuName(skuInfo.getSkuName());
                }
            } catch (Exception e) {
                log.error("调用远程服务gulimall-product查询skuinfo失败");
            }
            return this.save(wareSkuEntity);
        }
        // 只修改库存
        wareSkuEntity.setId(entity.getId());
        wareSkuEntity.setStock(entity.getStock() + num);
        return this.updateById(wareSkuEntity);
    }

    @Override
    public List<SkuStockTO> getSkusStock(List<Long> skuIds) {
        List<SkuStockTO> collect = skuIds.stream().map(skuId -> {
            SkuStockTO skuStockTO = new SkuStockTO();
            Long stock = baseMapper.getSkuStock(skuId);
            skuStockTO.setSkuId(skuId);
            skuStockTO.setStock(stock == null ? 0 : stock);
            return skuStockTO;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public Long getSkuStock(Long skuId) {
        return baseMapper.getSkuStock(skuId);
    }

    private boolean isValidId(String key) {
        return !StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(key);
    }

}