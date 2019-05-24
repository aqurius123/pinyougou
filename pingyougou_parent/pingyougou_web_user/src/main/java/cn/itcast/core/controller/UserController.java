package cn.itcast.core.controller;

import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import cn.itcast.core.utils.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    UserService userService;

    /**
     * 发送验证码
     */
    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
       //判断手机号码的合法性
        if (PhoneFormatCheckUtils.isPhoneLegal(phone)){
            //手机号码合法
            try {
                userService.sendCode(phone);
                return new Result(true, "发送成功");
            } catch (Exception e) {
                e.printStackTrace();
                return new Result(false, "发送失败");
            }
        }else {
            //手机号码不合法
            return new Result(false, "手机号码不正确");
        }
    }

    /**
     * 用户注册
     */
    @RequestMapping("/add")
    public Result add(@RequestBody User user, String smscode){
        try {
            userService.add(user, smscode);
            return new Result(true, "注册成功");
        }catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "注册失败");
        }
    }


}
