package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import entity.Cart;

import java.util.List;

public interface CartService {

    Item findItemById(Long itemId);

    List<Cart> findAllCartList(List<Cart> cartList);

    void addCartToRedis(String name, List<Cart> cartList);

    List<Cart> findAllCartListFromRedis(String name);
}
