package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家管理
 *
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference
    SellerService sellerService;
    /**
     * 入驻申请
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Seller seller){

        /*
            用户密码加密
         */
        //加密器
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        seller.setPassword(passwordEncoder.encode(seller.getPassword()));
        try {
            sellerService.add(seller);
            return new Result(true, "入驻成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "入驻失败");
        }
    }


}
