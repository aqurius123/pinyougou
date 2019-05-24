package cn.itcast.core.controller;

import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付管理
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    PayService payService;
    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map<String,String> createNative(){
        //获取当前用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return payService.createNative(name);
    }

    /**
     * 查询订单状态
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        try {
            int start = 0;
            while (true){
                Map<String,String> map = payService.queryPayStatus(out_trade_no);
                //判断响应的状态
                if ("NOTPAY".equals(map.get("trade_state"))){
                    //未支付
                    //每隔3秒查询循环问一次，直至超过5分钟,则关闭订单
                    Thread.sleep(3000);
                    start++;
                    if (start > 100){//超过5分钟
                        ////调用 关闭订单API  同学完成
//                        payService.closeOrder(out_trade_no);

                        return new Result(false, "支付超时");
                    }
                }else {
                    //支付成功
                    //已支付  修改 支付日志表  银行流水号  状态0-1  支付时间  当前时间..... 同学完成
                    return new Result(true, "支付成功");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "支付失败");
        }
    }

}
