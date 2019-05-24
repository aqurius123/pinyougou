package cn.itcast.core.listener;

import cn.itcast.core.service.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 监听器
 * 实现监听信息，进行页面静态化处理
 */
public class PageListener implements MessageListener {

    //注入静态化页面实现类
    @Autowired
    StaticPageService staticPageService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage mqTextMessage = (ActiveMQTextMessage)message;
        //接收数据信息。获取商品id
        try {
            String id = mqTextMessage.getText();
            //页面静态化处理
            //3;将商品信息进行静态化处理
            //因为静态化处理流程比较复杂，所以进行封装到一个实现类中
            //需要注入实现类对象
          staticPageService.index(Long.parseLong(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
