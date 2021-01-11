package com.vivi.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vivi.common.to.BrandTO;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.Query;
import com.vivi.gulimall.product.dao.BrandDao;
import com.vivi.gulimall.product.dao.CategoryBrandRelationDao;
import com.vivi.gulimall.product.entity.BrandEntity;
import com.vivi.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {


    @Autowired
    private CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 从请求参数中拿出关键字
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        // 如果传了关键字
        // select * from pms_brand where brand_id = key or name like key
        // 否则就是查询全部
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper);

        return new PageUtils(page);
    }

    /**
     * 默认生成的updateById()只会更新存储brand信息的表
     * 实际上可能在其他表中和此表有关联，通常会有一个brand_id字段，关联真正的brand表
     * 但是如果只存brand_id，需要其他信息再去查，就会总成数据库压力大，所以通常会伴随有几个冗余字段，比如brand_name
     * 所以在更新brand表的时候，如果更新字段部分包括了出现在其他表中的冗余字段，则需要将这些关联的表的这些部分也更新了
     * 这样才能保证数据一致性
     * @param brand
     */
    @Override
    public boolean updateCascadeById(BrandEntity brand) {
        // 先更新brand表本身
        this.updateById(brand);

        // 判断更新字段部分是否包括了出现在其他表中的冗余字段，

        // brand_cagegory_relation表中存在brand_id关联了brand表，并有冗余字段brand_name
        if (!StringUtils.isEmpty(brand.getName())) {
            // 更新brand_cagegory_relation表中的brand_name冗余字段
            categoryBrandRelationDao.updateBrandName(brand.getBrandId(), brand.getName());
        }

        // TODO 其他有关表中相关冗余字段的判断以及更新
        return true;
    }

    @Override
    public List<BrandTO> getBatch(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return this.baseMapper.getBatch(ids);
    }

}