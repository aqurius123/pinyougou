package cn.itcast.core.service;


import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService{


    @Autowired
    private ItemDao itemDao;
    @Autowired
    RedisTemplate redisTemplate;

    //根据id查询库存
    @Override
    public Item findItemById(Long itemId) {
        return itemDao.selectByPrimaryKey(itemId);
    }

    //购物车装满
    @Override
    public List<Cart> findAllCartList(List<Cart> cartList) {
        if (cartList.size() > 0){
            //遍历购物车集合，进行数据设置
            for (Cart cart : cartList) {
                //商家名称
                //订单详情集合
                List<OrderItem> orderItemList = cart.getOrderItemList();
                //库存ID 数量
                for (OrderItem orderItem : orderItemList) {
                    //根据id查询库存对象
                    Item item = findItemById(orderItem.getItemId());
                    //图片
                    orderItem.setPicPath(item.getImage());
                    //标题
                    orderItem.setTitle(item.getTitle());
                    //单价
                    orderItem.setPrice(item.getPrice());
                    //小计
                    orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum()));
                    //商家名称
                    cart.setSellerName(item.getSeller());
                }

            }
        }
        return cartList;
    }

    //添加购物车到缓存
    @Override
    public void addCartToRedis(String name, List<Cart> newCartList) {
        //先从缓存中获取购物车集合
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
        //合并新、老购物车集合。并最终合并到老购物车集合中
        oldCartList = mergeCartList(oldCartList, newCartList);
        //将合并后的购物车集合再次存储到redis缓存，覆盖掉之前的缓存
        redisTemplate.boundHashOps("CART").put(name, oldCartList);
    }


    public List<Cart> mergeCartList(List<Cart> oldCartList, List<Cart> newCartList) {
        //判断新车集合是否为空
        if (null != newCartList && newCartList.size() > 0){
            //判断老车集合是否为空
            if (null != oldCartList && oldCartList.size() > 0){
                //新老车大合并
                //1:判断新购物车中商家是否在老购物车集合中已经有了(本质上商家就是购物车)
                for (Cart newCart : newCartList) {
                    //判断新购物车集合中的商家是否在老购物车集合，其实就是判断新购车集合中的购物车是否已经
                    //存在于老购物车集合中
                    int is_exist = oldCartList.indexOf(newCart);
                    if (is_exist != -1){
                        /*
                            根据存在的角标，从老购物车集合获取老购物车
                         */
                        Cart oldCart = oldCartList.get(is_exist);
                        //获取老购物车中的订单详情集合
                        List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                        //老购物车集合已经有了新购物车
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        for (OrderItem newOrderItem : newOrderItemList) {
                            //判断新的购物车中的订单详情是否存在于老购物车集合的购物车
                            int index = oldOrderItemList.indexOf(newOrderItem);
                            if (index != -1){
                                //更新订单详情数量
                                 /*
                                    根据存在的角标，从老购物车订单集合获取老购物车订单详情
                                  */
                                OrderItem oldOrderItem = oldOrderItemList.get(index);
                                oldOrderItem.setNum(oldOrderItem.getNum()+newOrderItem.getNum());
                            }else {
                                oldOrderItemList.add(newOrderItem);
                            }
                        }

                    }else {
                        //老购物车集合不存在新购物车
                        //直接将新购物车添加到老购物车集合
                        oldCartList.add(newCart);
                    }
                }
            }else {
                //老购物车位空，直接添加新购物车集合
                return newCartList;
            }
        }else {
            return oldCartList;
        }
        return oldCartList;
    }

    //从合并后的缓存中取出购物车集合
    @Override
    public List<Cart> findAllCartListFromRedis(String name) {
        return (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
    }
}
