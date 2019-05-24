package cn.itcast.core.controller;

import cn.itcast.core.pojo.user.User;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录管理
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    //方法一：直接从session中获取
    @RequestMapping("/showName")
//    public Map showName(HttpSession httpSession){
//        SecurityContext securityContext = (SecurityContext) httpSession.getAttribute("SPRING_SECURITY_CONTEXT");
//        User user = (User) securityContext.getAuthentication().getPrincipal();
//        String name = user.getName();
//        Map<String,String> map = new HashMap<>();
//        map.put("username", name);
//        return map;
//
//    }
    //方法二：使用安全框架
//    public Map showName(){
//        String name = SecurityContextHolder.getContext().getAuthentication().getName();
//        Map map = new HashMap();
//        map.put("username", name);
//        return map;
//    }
    public Map<String, Object> showName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> map = new HashMap<>();
        map.put("username", name);
        map.put("nowDate", new Date());
        return map;
    }
}
