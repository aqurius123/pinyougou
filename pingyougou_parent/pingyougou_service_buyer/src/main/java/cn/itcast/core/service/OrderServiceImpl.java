package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.utils.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    OrderDao orderDao;
    @Autowired
    OrderItemDao orderItemDao;
    @Autowired
    IdWorker idWorker;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ItemDao itemDao;
    @Autowired
    PayLogDao payLogDao;
    //提交订单
    @Override
    public void submitOrder(Order order) {

        //定义支付订单id列表
        List<String> ids = new ArrayList<>();
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(order.getUserId());
                //变量购物车集合，获取每一个购物车
        //定义实付总金额
        long totalPay = 0;
        for (Cart cart : cartList) {
            long id = idWorker.nextId();
            ids.add(String.valueOf(id));
            order.setOrderId(id);
            //获取购物项列表
            List<OrderItem> orderItemList = cart.getOrderItemList();
            //变量购物项列表，获取每个商品对象
            //定义小计变量
            double minTotal = 0;
            for (OrderItem orderItem : orderItemList) {
                orderItem.setId(idWorker.nextId());
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                //商品ID
                orderItem.setGoodsId(item.getGoodsId());
                //标题
                orderItem.setTitle(item.getTitle());
                //单价
                orderItem.setPrice(item.getPrice());
                //小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()* orderItem.getNum()));
                //订单ID
                orderItem.setOrderId(idWorker.nextId());
                //图片
                orderItem.setPicPath(item.getImage());
                //商家ID
                orderItem.setSellerId(item.getSellerId());
                //小计之和
                minTotal += orderItem.getTotalFee().doubleValue();
                //保存订单详情表
                orderItemDao.insertSelective(orderItem);
             }




            //考虑到后期可能需要做大量数据分析，要求该订单号在全国乃至全球必须是唯一的
            //使用分布式ID生成器，生成不重复的点单id
            //保存订单表
            //收件人
            //收货地址
            //收货号码
            //实付金额
            order.setPayment(new BigDecimal(minTotal));
            //总付金额，单位为分
            totalPay += order.getPayment().doubleValue()*100;
            //状态
            order.setStatus("0");
            //添加时间
            order.setCreateTime(new Date());
            //更新时间
            order.setCreateTime(new Date());
            //订单来源
            order.setInvoiceType("2");
            //商家ID
            order.setSellerId(cart.getSellerId());

        }
        //保存订单
        orderDao.insertSelective(order);

        //保存支付日志表
        PayLog payLog = new PayLog();
        //保存out_trade_no
        payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
        //生成日期
        payLog.setCreateTime(new Date());
        //总付金额
        payLog.setTotalFee(totalPay);
        //用户id
        payLog.setUserId(order.getUserId());
        //支付状态   0:未支付    1：已支付
        payLog.setTradeState("0");
        //订单字符串列表
        payLog.setOrderList(ids.toString().replace("[","").replace("]",""));
        //支付类型
        payLog.setPayType("1");

        //保存支付日志表
        payLogDao.insertSelective(payLog);

        //将订单保存到缓存中,用字符串类型更好，因为可以设置有效时间
//        redisTemplate.boundHashOps("CART").put(order.getUserId(), payLog);
        redisTemplate.boundValueOps(order.getUserId()).set(payLog,24, TimeUnit.HOURS);

        /*
            提交订单后，清空购物车
         */
        //redisTemplate.delete("CART");     //不合适，会将所有用户购物车都清空
        //清空当前 用户购物车,为了做测试，先不删除购物车
//        redisTemplate.boundHashOps("CART").delete(order.getUserId());

    }
}
