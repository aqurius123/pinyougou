package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.StaticPageService;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

//import javax.security.auth.login.AppConfigurationEntry;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 处理静态化页面
 */
@Service
public class StaticPageServiceImpl implements StaticPageService,ServletContextAware {
    @Autowired
    FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    ItemDao itemDao;
    @Autowired
    GoodsDescDao goodsDescDao;
    @Autowired
    GoodsDao goodsDao;
    @Autowired
    ItemCatDao itemCatDao;
    public void index(Long id){

        Configuration conf = freeMarkerConfigurer.getConfiguration();

        //输出路径  此时 绝对路径
        String path = getPath("/"+id+".html");
        //加载模板
        Writer out = null;
        try {
            // 读取
            Template template = conf.getTemplate("item.ftl");
            //数据
            Map<String,Object> root = new HashMap<>();

            //根据商品ID 外键   查询库存结果集
            ItemQuery itemQuery = new ItemQuery();
            itemQuery.createCriteria().andGoodsIdEqualTo(id);
            List<Item> itemList = itemDao.selectByExample(itemQuery);
            root.put("itemList",itemList);
            //根据商品ID 查询商品详情对象
            GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
            root.put("goodsDesc",goodsDesc);


            //根据商品ID 查询商品对象
            Goods goods = goodsDao.selectByPrimaryKey(id);
            root.put("goods",goods);
            //查询一级 二级 三级分类的名称
            root.put("itemCat1",itemCatDao.selectByPrimaryKey(goods.getCategory1Id()).getName());
            root.put("itemCat2",itemCatDao.selectByPrimaryKey(goods.getCategory2Id()).getName());
            root.put("itemCat3",itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());

            //输出  写 "UTF-8"
            out = new OutputStreamWriter(new FileOutputStream(path),"UTF-8");
            //处理
            template.process(root,out);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != out){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    //获取全路径
    public String getPath(String path){
        return servletContext.getRealPath(path);
    }

    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
