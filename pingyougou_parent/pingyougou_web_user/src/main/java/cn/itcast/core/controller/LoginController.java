package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录管理
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * 查询当前用户名并展示
     * @return
     */
    @RequestMapping("/name")
    public Map<String,String> showName(){
        Map<String,String> map = new HashMap<>();
        //获取用户名
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName", loginName);
        return map;
    }
}
