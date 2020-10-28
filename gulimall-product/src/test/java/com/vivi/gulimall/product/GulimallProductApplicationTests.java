package com.vivi.gulimall.product;

import com.vivi.gulimall.product.entity.BrandEntity;
import com.vivi.gulimall.product.entity.CategoryEntity;
import com.vivi.gulimall.product.service.BrandService;
import com.vivi.gulimall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.security.RunAs;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Autowired
	CategoryService categoryService;
	
	@Autowired
	private StringRedisTemplate redisTemplate;

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

	@Test
	void testCategoryFindCatgoryPath() {
		List<Long> path = categoryService.findCategoryPath((long) 225);
		System.out.println(Arrays.asList(path));
		System.out.println(path);
		path.forEach(System.out::println);
	}

	@Test
	void testStringRedisTemplate() {
		String res = redisTemplate.opsForValue().get("hello");
		System.out.println(res);
	}

}
