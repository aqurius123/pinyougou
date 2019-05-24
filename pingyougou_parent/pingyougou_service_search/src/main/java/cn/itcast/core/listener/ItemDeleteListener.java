package cn.itcast.core.listener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 监听器，实现删除索引库数据信息
 */
public class ItemDeleteListener implements MessageListener {

    @Autowired
    SolrTemplate solrTemplate;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage mqTextMessage = (ActiveMQTextMessage)message;
        //接收信息
        try {
            String id = mqTextMessage.getText();
            //从索引库删除数据信息
            //2：从索引库中删除商品信息
            SolrDataQuery solrDataQuery = new SimpleQuery();
            solrDataQuery.addCriteria(new Criteria("item_goodsid").is(id));
            solrTemplate.delete(solrDataQuery);
            solrTemplate.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
