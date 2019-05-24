package cn.itcast.core.service;


import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;


import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    JmsTemplate jmsTemplate;
    //注入发送目的地
    @Autowired
    private Destination smsDestination;
    @Autowired
    UserDao userDao;
    //发送验证码
    @Override
    public void sendCode(String phone) {
        //生成6位随机数验证码
        String code = RandomStringUtils.randomNumeric(6);
        //保存验证码到缓存中(用于当用户提交注册时进行验证码比较)
        redisTemplate.boundValueOps(phone).set(code);
        //设置缓存有效时间
        redisTemplate.boundValueOps(phone).expire(2, TimeUnit.HOURS);
        //发消息
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("PhoneNumbers",phone);
                mapMessage.setString("SignName","友情岁月笙");
                mapMessage.setString("TemplateCode","SMS_165410662");
//                mapMessage.setString("TemplateParam","{'code:'"+code+"}");
                mapMessage.setString("TemplateParam","{\"code\":"+code+"}");
                return mapMessage;
            }
        });
    }

    //用户注册
    @Override
    public void add(User user, String smscode) {
        //确认用户有输入验证码
        if (null != smscode){
            //从缓存中获取验证码，并比较
            String realCode = (String) redisTemplate.boundValueOps(user.getPhone()).get();
            /*
                这一点忘记了:从缓存中获取验证码，一定要进行判断是否为空
             */
            if (null != realCode){
                if (smscode.equals(realCode)){
                    //保存用户信息
                    user.setCreated(new Date());
                    user.setUpdated(new Date());
                    userDao.insertSelective(user);
                }else {
                    //可能输入验证码错误
                    throw  new RuntimeException("您输入的验证码有误");
                }

            }else {
                //从缓存中未获取到验证码。可能失效
                throw new RuntimeException("验证码已过期，请重新获取验证码");
            }
        }
    }
}
