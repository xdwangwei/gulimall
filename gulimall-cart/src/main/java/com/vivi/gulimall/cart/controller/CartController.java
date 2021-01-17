package com.vivi.gulimall.cart.controller;

import com.vivi.common.utils.R;
import com.vivi.gulimall.cart.service.CartService;
import com.vivi.gulimall.cart.vo.CartItemVO;
import com.vivi.gulimall.cart.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wangwei
 * 2021/1/16 12:31
 */
@Controller
@RequestMapping(value = "/cart")
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 用户访问其购物车
     * @return
     */
    @GetMapping("/list.html")
    public String cartList(Model model) {

        // 执行目标方法之前会被拦截，并保存登录状态
        // UserLoginStatusTO statusTO = CartInterceptor.threadLocal.get();

        CartVO cart = cartService.getCart();
        model.addAttribute("cart", cart);

        return "cartList";
    }

    /**
     * 用户将某个商品加入其购物车，成功后返回成功页面，
     * 为了避免表单重复提交，应该使用重定向
     *
     * 如果使用  redirectAttributes.addFlashAttribute("item", itemVO); 重定向回成功页面，只能取一次数据，用户刷新后就无了
     *
     * 所以应该重定向到另一个请求，单上当次的skuId，控制器重新获取这个购物项数据，放入model中，再返回到成功页
     *
     * @return
     */
    @GetMapping("/additem")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("count") Integer count) {
        CartItemVO itemVO = cartService.addToCart(skuId, count);
        return "redirect:http://cart.gulimall.com/cart/addToCart?skuId=" + skuId.toString();
    }

    /**
     * 结合上面那个方法，共同完成添加商品到购物车功能，即可刷新页面，又不会重复提交表单数据
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                                 Model model) {
        CartItemVO itemVO = cartService.getCartItem(skuId);
        model.addAttribute("item", itemVO);
        return "success";
    }

    @ResponseBody
    @PostMapping("/del/batch")
    public R delBatch(@RequestBody List<String> skuIds) {
        cartService.deleteBatch(skuIds);
        return R.ok();
    }
    /**
     * 改变购物车中某个购物项选中状态
     */
    @ResponseBody
    @GetMapping("/change/status")
    public R changeItemStatus(@RequestParam("skuId") String skuId,
                              @RequestParam("checked") Boolean checked) {
        cartService.changeItemStatus(skuId, checked);
        return R.ok();
    }

    /**
     * 改变购物车中某个购物项的数量
     */
    @ResponseBody
    @GetMapping("/change/count")
    public R changeItemCount(@RequestParam("skuId") String skuId,
                              @RequestParam("count") Integer count) {
        cartService.changeItemCount(skuId, count);
        return R.ok();
    }

}
