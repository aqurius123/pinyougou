package cn.itcast.core.controller;

import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单管理
 */
@RestController
@RequestMapping("/order")
public class OrderController {


    @Reference
    OrderService orderService;
    /**
     * 提交订单
     * @param order
     * @return
     */
    @RequestMapping("/add")
    public Result submitOrder(@RequestBody Order order){
        try {
            //获取用户id
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            order.setUserId(name);
            orderService.submitOrder(order);
            return new Result(true, "订单提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "订单提交失败");
        }

    }
}
