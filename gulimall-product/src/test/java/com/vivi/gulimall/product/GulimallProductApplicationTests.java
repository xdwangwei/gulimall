package com.vivi.gulimall.product;

import com.vivi.gulimall.product.entity.BrandEntity;
import com.vivi.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.security.RunAs;
import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Test
	void contextLoads() {

		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setName("华为");
		brandEntity.setDescript("中华有为");
		// 测试新增数据
		brandService.save(brandEntity);

		// 测试查找数据
		List<BrandEntity> brandEntities = brandService.list();
		brandEntities.forEach(System.out::println);
	}

}
