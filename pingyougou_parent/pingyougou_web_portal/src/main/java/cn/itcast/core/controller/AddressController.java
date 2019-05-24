package cn.itcast.core.controller;

import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.service.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地址管理
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Reference
    AddressService addressService;

    /**
     * 获取当前用户名的地址列表
     * @return
     */
    @RequestMapping("/findListByLoginUser")
    public List<Address> findListByLoginUser(){
        //获取当前用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findListByLoginUser(name);
    }

}
