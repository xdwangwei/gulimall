package com.vivi.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.vivi.common.constant.CartConstant;
import com.vivi.common.exception.BizCodeEnum;
import com.vivi.common.exception.BizException;
import com.vivi.common.to.SkuInfoTO;
import com.vivi.common.utils.R;
import com.vivi.gulimall.cart.feign.ProductFeignService;
import com.vivi.gulimall.cart.interceptor.CartInterceptor;
import com.vivi.gulimall.cart.service.CartService;
import com.vivi.gulimall.cart.vo.CartItemVO;
import com.vivi.gulimall.cart.vo.CartVO;
import com.vivi.gulimall.cart.vo.UserLoginStatusTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author wangwei
 * 2021/1/16 15:16
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 获取当前用户的购物车
     * - 如果是未登录用户，他只有user-key(临时购物车)，拿到全部购物项
     * - 如果是登录用户，他既有id(正式购物车)，又有user-key(临时购物车)，
     *      如果它的临时购物车不空，则要将其临时购物车中的购物项合并到正式购物车中，并清空临时购物车
     * @return
     */
    @Override
    public CartVO getCart() {
        CartVO cartVO = new CartVO();
        UserLoginStatusTO userLoginStatusTO = CartInterceptor.threadLocal.get();
        // 1. 先拿到临时购物车数据
        String tempCartKey = CartConstant.CART_REDIS_KEY_PREFIX + userLoginStatusTO.getUserKey();
        List<CartItemVO> tempCartItems = getCartItems(tempCartKey);
        // 1.1 临时用户，只有临时购物车，直接返回
        if (userLoginStatusTO.getId() == null) {
            cartVO.setItems(tempCartItems);
        // 1.2 登录用户，合并临时购物车到正式购物车，清空临时购物车，返回正式购物车
        } else {
            String userCartKey = CartConstant.CART_REDIS_KEY_PREFIX + userLoginStatusTO.getId();
            if (!CollectionUtils.isEmpty(tempCartItems)) {
                // 转移合并
                tempCartItems.forEach(tempCartItem -> moveToCertainCart(tempCartItem, userCartKey));
                // 清空临时购物车
                clearCart(tempCartKey);
            }
            // 重新获取该用户的正式购物车
            List<CartItemVO> cartItems = getCartItems(userCartKey);
            cartVO.setItems(cartItems);
        }
        return cartVO;
    }

    /**
     * 将商品添加到当前用户购物车
     * @param skuId
     * @param count
     * @return
     */
    @Override
    public CartItemVO addToCart(Long skuId, Integer count) {
        // 当前用户的购物车key
        String cartKey = getCurrentUserCartKey();
        return addToCertainCart(skuId, count, cartKey);
    }

    /**
     * 得到当前用户购物车中某个购物项的详细数据
     * @param skuId
     * @return
     */
    @Override
    public CartItemVO getCartItem(Long skuId) {
        // 得到当前登录用户在redis中的购物车数据的操作器
        BoundHashOperations<String, String, String> op = getCurrentUserCartOps();
        String dataStr = op.get(skuId.toString());
        if (!StringUtils.isEmpty(dataStr)) {
            CartItemVO itemVO = JSON.parseObject(dataStr, CartItemVO.class);
            return itemVO;
        }
        return null;
    }

    @Override
    public void deleteBatch(List<String> skuIds) {
        String currentUserCartKey = getCurrentUserCartKey();
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(currentUserCartKey);
        skuIds.forEach(skuId -> ops.delete(skuId));
    }

    @Override
    public void changeItemStatus(String skuId, Boolean checked) {
        BoundHashOperations<String, String, String> ops = getCurrentUserCartOps();
        String s = ops.get(skuId);
        if (!StringUtils.isEmpty(s)) {
            CartItemVO cartItem = JSON.parseObject(s, CartItemVO.class);
            cartItem.setChecked(checked);
            ops.put(skuId, JSON.toJSONString(cartItem));
        }
    }

    @Override
    public void changeItemCount(String skuId, Integer count) {
        BoundHashOperations<String, String, String> ops = getCurrentUserCartOps();
        String s = ops.get(skuId);
        if (!StringUtils.isEmpty(s)) {
            CartItemVO cartItem = JSON.parseObject(s, CartItemVO.class);
            cartItem.setCount(count);
            ops.put(skuId, JSON.toJSONString(cartItem));
        }
    }

    /**
     * 获取当前用户的购物车在redis中的key
     * @return
     */
    private String getCurrentUserCartKey() {
        UserLoginStatusTO userLoginStatusTO = CartInterceptor.threadLocal.get();
        // 未登录用户，其购物车数据在redis中的key
        if (userLoginStatusTO.getId() == null) {
            return CartConstant.CART_REDIS_KEY_PREFIX + userLoginStatusTO.getUserKey();
        } else {
            // 已登录
            return CartConstant.CART_REDIS_KEY_PREFIX + userLoginStatusTO.getId().toString();
        }
    }

    /**
     * 获取当前用户的购物车在redis中的操作器
     */
    private BoundHashOperations<String, String, String> getCurrentUserCartOps() {
        return redisTemplate.boundHashOps(getCurrentUserCartKey());
    }

    /**
     * 获取指定购物车的数据
     * @param cartKey
     * @return
     */
    private List<CartItemVO> getCartItems(String cartKey) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(cartKey);
        List<String> values = operations.values();
        List<CartItemVO> collect = values.stream().map(value -> JSON.parseObject(value, CartItemVO.class)).collect(Collectors.toList());
        return collect;
    }

    /**
     * 根据sku'id查出该商品的基本信息、销售属性，构造购物车项，
     * @param skuId
     * @return
     */
    private CartItemVO buildCartItemVO(Long skuId) {
        CartItemVO itemVO = new CartItemVO();
        itemVO.setSkuId(skuId);
        // 使用线程池异步编排提高效率
        CompletableFuture<Void> skuInfoTask = CompletableFuture.runAsync(() -> {
            // 调用远程服务查询sku详情
            R res = productFeignService.getSkuInfo(skuId);
            if (res.getCode() != 0) {
                log.error("远程调用gulimall-product查询skuinfo失败");
                throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED, "加入购物车失败");
            } else {
                SkuInfoTO skuInfo = res.getData("skuInfo", SkuInfoTO.class);
                itemVO.setPrice(skuInfo.getPrice());
                itemVO.setSkuTitle(skuInfo.getSkuTitle());
                itemVO.setSkuImg(skuInfo.getSkuDefaultImg());
            }
        }, executor);

        CompletableFuture<Void> saleAttrTask = CompletableFuture.runAsync(() -> {
            // 调用远程服务查询sku saleattrs
            R r = productFeignService.getSaleAttrStringList(skuId);
            if (r.getCode() != 0) {
                log.error("远程调用gulimall-product查询sku saleattr失败");
                throw new BizException(BizCodeEnum.CALL_FEIGN_SERVICE_FAILED);
            } else {
                itemVO.setAttrs(r.getData(new TypeReference<List<String>>() {
                }));
            }
        }, executor);
        // 等待异步任务执行完成
        try {
            CompletableFuture.allOf(skuInfoTask, saleAttrTask).get();
        } catch (Exception e) {
            log.error("线程池异步编排构造购物项详细信息失败");
            throw new BizException(BizCodeEnum.THREAD_POOL_TASK_FAILED);
        }
        return itemVO;
    }

    /**
     * 将商品加入指定的购物车中
     * @return
     */
    private CartItemVO addToCertainCart(Long skuId, Integer count, String cartKey) {
        // 判断购物车中该商品是否存在
        // 如果购物车中没有这个商品，那就是新加；如果有，那就是修改数量
        BoundHashOperations<String, String, String> op = redisTemplate.boundHashOps(cartKey);
        String dataStr = op.get(skuId.toString());
        // 有
        if (!StringUtils.isEmpty(dataStr)) {
            CartItemVO itemVO = JSON.parseObject(dataStr, CartItemVO.class);
            itemVO.setCount(itemVO.getCount() + count);
            op.put(skuId.toString(), JSON.toJSONString(itemVO));
            return itemVO;
        }
        // 没有
        CartItemVO itemVO = buildCartItemVO(skuId);
        itemVO.setCount(count);
        itemVO.setChecked(true);
        op.put(skuId.toString(), JSON.toJSONString(itemVO));
        return itemVO;
    }

    /**
     * 将商品合并到指定的购物车中
     *
     * 主要用于 合并临时购物车到正式购物车
     * @return
     */
    private void moveToCertainCart(CartItemVO itemVO, String cartKey) {
        // 判断购物车中该商品是否存在
        // 如果购物车中没有这个商品，那就是新加；如果有，那就是修改数量
        BoundHashOperations<String, String, String> op = redisTemplate.boundHashOps(cartKey);
        String dataStr = op.get(itemVO.getSkuId().toString());
        // 有
        if (!StringUtils.isEmpty(dataStr)) {
            CartItemVO cartItem = JSON.parseObject(dataStr, CartItemVO.class);
            cartItem.setCount(cartItem.getCount() + itemVO.getCount());
            op.put(cartItem.getSkuId().toString(), JSON.toJSONString(cartItem));
        } else {
            // 没有，直接挪进去
            op.put(itemVO.getSkuId().toString(), JSON.toJSONString(itemVO));
        }
    }

    /**
     * 清空指定购物车
     * @param cartKey
     */
    private void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

}
