package entity;

import cn.itcast.core.pojo.order.OrderItem;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 自定义购物车实体类
 * 必须实现序列化接口，因为采用的是doubbox远程调用服务
 */
public class Cart implements Serializable{

    private String sellerId;   //商家id
    private String sellerName;  //商家名称
    private List<OrderItem> orderItemList;     //订单详情列表

    public Cart() {
    }

    public Cart(String sellerId, String sellerName, List<OrderItem> orderItemList) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.orderItemList = orderItemList;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(sellerId, cart.sellerId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sellerId);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "sellerId='" + sellerId + '\'' +
                ", sellerName='" + sellerName + '\'' +
                ", orderItemList=" + orderItemList +
                '}';
    }
}
