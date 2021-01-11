package com.vivi.gulimall.product.dao;

import com.vivi.common.to.BrandTO;
import com.vivi.gulimall.product.entity.BrandEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vivi.gulimall.product.vo.BrandVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌
 * 
 * @author wanwgei
 * @email i@weiwang.com
 * @date 2020-09-13 10:48:45
 */
@Mapper
public interface BrandDao extends BaseMapper<BrandEntity> {

    List<BrandTO> getBatch(@Param("ids") List<Long> ids);
}
