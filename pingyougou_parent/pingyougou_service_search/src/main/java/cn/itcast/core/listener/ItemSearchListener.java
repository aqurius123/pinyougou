package cn.itcast.core.listener;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.List;

/**
 * 消费者监听器
 * 必须实现MessageListener接口
 */
public class ItemSearchListener implements MessageListener {

    @Autowired
    ItemDao itemDao;
    @Autowired
    SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
        //接收消息
        //使用Messages接口的实现类对象
        ActiveMQTextMessage mqTextMessage = (ActiveMQTextMessage)message;
        //获取传递的消息 商品id
        String id = null;
        try {
            id = mqTextMessage.getText();
            //2:将商品信息保存到索引库
            //查询库存表数据信息，并保存到索引库
            ItemQuery itemQuery = new ItemQuery();
            itemQuery.createCriteria().andGoodsIdEqualTo(Long.parseLong(id)).andIsDefaultEqualTo("1");
            List<Item> itemList = itemDao.selectByExample(itemQuery);
            solrTemplate.saveBeans(itemList);
        /*
            务必记得提交
         */
            solrTemplate.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }



    }
}
