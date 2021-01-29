package com.vivi.gulimall.order.web;

import com.vivi.common.to.OrderTO;
import com.vivi.common.utils.Constant;
import com.vivi.common.utils.PageUtils;
import com.vivi.common.utils.R;
import com.vivi.gulimall.order.service.OrderService;
import com.vivi.gulimall.order.vo.OrderConfirmVO;
import com.vivi.gulimall.order.vo.OrderCreateVO;
import com.vivi.gulimall.order.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangwei
 * 2021/1/18 13:50
 */
@Controller
public class WebController {

    @Autowired
    OrderService orderService;

    /**
     * 用户订单列表
     * @return
     */
    @GetMapping("/center/list.html")
    public String orderList(@RequestParam(value = "pageNum", required = false, defaultValue = "1") String pageNum,
                            @RequestParam(value = "pageSize", required = false, defaultValue = "4") String pageSize,
                            Model model) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.PAGE, pageNum);
        params.put(Constant.LIMIT, pageSize);
        PageUtils page = orderService.getCurrentUserOrderList(params);
        model.addAttribute("page", page);
        return "list";
    }

    /**
     * 用户从购物车页面点击去结算
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        // 得到用户本次结算时购物车中选中的那些购物项
        OrderConfirmVO orderConfirmVO = orderService.confirmOrder();
        model.addAttribute("order", orderConfirmVO);
        return "confirm";
    }

    /**
     * 提交订单，创建订单
     *
     */
    @PostMapping("/submit")
    public String submit(OrderSubmitVO submitVO) {
        OrderCreateVO resp = orderService.submit(submitVO);
        return "redirect:http://order.gulimall.com/topay?orderSn=" + resp.getOrder().getOrderSn();
    }

    @GetMapping("/topay")
    public String topay(@RequestParam("orderSn") String orderSn,
                        Model model) {
        OrderCreateVO resp = orderService.getOrderDetail(orderSn);
        model.addAttribute("orderCreateVO", resp);
        return "pay";
    }

    /**
     * 以/api开始的为远程调用，需要被拦截器直接放行
     * @param orderSn
     * @return
     */
    @ResponseBody
    @GetMapping("/api/info/{orderSn}")
    public R getOrderDetail(@PathVariable("orderSn") String orderSn) {
        OrderTO resp = orderService.getOrderTOByOrderSn(orderSn);
        return R.ok().setData(resp);
    }
}
