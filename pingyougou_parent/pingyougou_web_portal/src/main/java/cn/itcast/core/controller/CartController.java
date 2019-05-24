package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车管理
 * 直接操作库存表，因为库存表几乎包含商品所有数据信息
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    CartService cartService;

    /**
     * 添加 购物车
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = {"http://localhost:9003"}, allowCredentials = "true")    //解决跨域问题
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {
        try {
            List<Cart> cartList = null;
            //定义一个布尔值标记cookie是否有数据
            boolean flag = false;
            //1:获取Cookie
            Cookie[] cookies = request.getCookies();
            if (null != cookies && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    //2:获取Cookie中购物车集合
                    if ("CART".equals(cookie.getName())) {
                        //证明cookie中有数据，获取出来，并修改标记状态
                        cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                        flag = true;
                    }
                }
            }
            //3:没有 创建购物车
            if (null == cartList) {
                cartList = new ArrayList<>();
            }
//            4:追加当前款
            Cart newCart = new Cart();
            //创建新的订单详情对象
            OrderItem newOrderItem = new OrderItem();
            //库存ID
            newOrderItem.setItemId(itemId);
            //数量
            newOrderItem.setNum(num);
            //创建订单详情集合
            List<OrderItem> newOrderItemList = new ArrayList<>();
            newOrderItemList.add(newOrderItem);
            newCart.setOrderItemList(newOrderItemList);

            //根据库存ID查询 商家ID
            Item item = cartService.findItemById(itemId);

            newCart.setSellerId(item.getSellerId());


            //1:判断新购物车中商家是否在老购物车集合中已经存在
            int indexOf = cartList.indexOf(newCart);   // indexOf 不存在是-1  存在返回角标
            if (indexOf != -1) {
                //-- 存在
                //2:判断新购物车中的新订单详情在    从老购物车集合中找出跟新购物车相同商家的老购物车的老订单详情集合中是否存在
                Cart oldCart = cartList.get(indexOf); //此老车就和新车是同商家
                List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                int i = oldOrderItemList.indexOf(newOrderItem);
                if (i != -1) {
                    //-- 存在   追加数量
                    OrderItem oldOrderItem = oldOrderItemList.get(i);
                    oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                } else {
                    //-- 不存在  直接添加
                    oldOrderItemList.add(newOrderItem);
                }

            } else {
                //-- 不存在 直接添加
                cartList.add(newCart);
            }
            //判断用户是否已经登录还是未登录（也叫匿名登录）
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!"anonymousUser".equals(name)){
                //已登录
                //1:获取Cookie
                //2:获取Cookie中购物车集合
                //3:没有 创建购物车
                //4:追加当前款
                //5:将合并后的购物车合并到Redis缓存中
                cartService.addCartToRedis(name,cartList);
                //6: 清空Cookie 回写浏览器
                 /*
                    严谨一些：如果cookie没有数据，就不需要进行清空操作
                 */
                 if (flag){
                    Cookie cookie = new Cookie("CART",null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                 }


            }else {
    //            未登陆
                //1:获取Cookie
                //2:获取Cookie中购物车集合
                //3:没有 创建购物车
                //4:追加当前款
                //5:创建Cookie添加购物车集合
                    Cookie cookie = new Cookie("CART", JSON.toJSONString(cartList));
                    cookie.setMaxAge(60 * 60 * 24 * 365);
                    cookie.setPath("/");
    //              6:回写浏览器
    //              URLEncoder.encode(cookie.getName(), "utf-8");
                    response.addCookie(cookie);

            }
            return new Result(true, "加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "加入购物车失败");
        }
    }


    /**
     * 查询购物车列表
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response){

        //未登陆
        List<Cart> cartList = null;
        //1:获取Cookie
        Cookie[] cookies = request.getCookies();
        if (null != cookies && cookies.length > 0){
            for (Cookie cookie : cookies) {
                //2:获取Cookie中购物车集合
                /*
                    这一步判断忘记了
                 */
                if ("CART".equals(cookie.getName())){  //
                    cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                    /*
                        之所以使用break，是因为当cookie的长度较大时，
                        如果遍历的时候找到了购物车，就没必要继续遍历了
                     */
                    break;
                }
            }
        }

        //判断是否登陆 是否获取当前登陆人的用户名        空指针异常(安全框架默认的是匿名用户登录，除非真正用户登录)
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(name)){  //判断是否匿名登陆，从而判断用户是否已经登录
        //登陆了
            if (null != cartList){
//            3:有 将此购物车合并到Redis缓存中  清空Cookie 回写浏览器
                cartService.addCartToRedis(name,cartList);
                //清空cookie
                Cookie cookie = new Cookie("CART",null);
                cookie.setMaxAge(0);    //0:立即销毁    -1：关闭浏览器即销毁   >0： 指定时间后销毁
                cookie.setPath("/");
                //一定要将cookie更新到response中
                response.addCookie(cookie);
            }
//            4: 从缓存中将购物车集合查询出来
            cartList = cartService.findAllCartListFromRedis(name);
        }


//        5:有购物车集合 ，则将购物车装满
        if (null != cartList){
            //查询所有购物车集合
            /*
                这个地方有点不好理解
             */
            cartList = cartService.findAllCartList(cartList);
        }
        //6:回显
        return cartList;
    }
}