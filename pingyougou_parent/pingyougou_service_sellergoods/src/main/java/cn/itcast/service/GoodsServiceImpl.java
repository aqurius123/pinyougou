package cn.itcast.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.StaticPageService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.GoodsVo;
import entity.PageResult;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import sun.plugin.liveconnect.SecureInvocation;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {


    //按条件，分页查询
    @Autowired
    GoodsDao goodsDao;
    @Autowired
    GoodsDescDao goodsDescDao;
    @Autowired
    ItemDao itemDao;
    @Autowired
    ItemCatDao itemCatDao;
    @Autowired
    SellerDao sellerDao;
    @Autowired
    BrandDao brandDao;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    Destination topicPageAndSolrDestination;

    //注入删除索引库目的地
    @Autowired
    Destination queueSolrDeleteDestination;
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {

        //开启分页
        PageHelper.startPage(page,rows);
        //排序(根据id，指定排序方式)
        PageHelper.orderBy("id desc");
        //查询所有
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        //根据状态条件查询
        if (null != goods.getAuditStatus() && !"".equals(goods.getAuditStatus())){
            criteria.andAuditStatusEqualTo(goods.getAuditStatus());
        }
        //判断是否有指定模糊条件查询
        if (null != goods.getGoodsName() && !"".equals(goods.getGoodsName().trim())){
            criteria.andGoodsNameLike("%"+goods.getGoodsName().trim()+"%");
        }
        //查询商家自己的商品列表(从后台controller层获取sellerId)
        if (null != goods.getSellerId()){
            criteria.andSellerIdEqualTo(goods.getSellerId());
        }

        //查询未删除的商品列表
        criteria.andIsDeleteIsNull();
        List<Goods> goodsList = goodsDao.selectByExample(goodsQuery);
        PageInfo<Goods> pageInfo = new PageInfo<>(goodsList);
        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }

    //添加商品（提交的数据需要保存到多张表）
    @Override
    public void add(GoodsVo goodsVo) {

        //保存商品表
            //商品的状态   默认是0：未审核     1：审核通过    2：审核未通过   3：关闭
        goodsVo.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsVo.getGoods());

        //保存商品详情表
        /*
            因为商品详情表和商品表是公用的同一个主键，所有需要获取到上一次保存记录的id（也就是商家id，其实也就是商家名称）
            这个地方容易忽略
         */
        goodsVo.getGoodsDesc().setGoodsId(goodsVo.getGoods().getId());
        goodsDescDao.insertSelective(goodsVo.getGoodsDesc());
        //保存库存表（规格表）----暂时放下
        //判断是否启用规格
        if ("1".equals(goodsVo.getGoods().getIsEnableSpec())){//启用规格
            //保持多个库存表
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
                //对应数据库给库存表保持字段数据
                //保存标题（标题=商品名称+“ ”+ 规格1+ “ ”+ 规格2+。。。）
                String title = goodsVo.getGoods().getGoodsName();
                //规格{"机身内存":"16G","网络":"联通3G"}----json串
                Map<String,String> specMap = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entrySet = specMap.entrySet();
                for (Map.Entry<String, String> entry : entrySet) {
                    title += " "+ entry.getValue();
                }
                item.setTitle(title);
                setAttribute(goodsVo, item);
                //保持库存表所有数据
//                System.out.println(item.toString());
                itemDao.insertSelective(item);
            }
        }else {//不启用规格，默认有一份
            //不启用  默认
            Item item = new Item();
            //标题
            item.setTitle(goodsVo.getGoods().getGoodsName());
            //设置库存的属性
            setAttribute(goodsVo,item);
            //规格
            item.setSpec("{}");
            //价格
            item.setPrice(new BigDecimal(0));
            //库存
            item.setNum(9999);
            //状态 是否启用
            item.setStatus("1");

            itemDao.insertSelective(item);

        }

    }

    public void setAttribute(GoodsVo goodsVo, Item item) {

        //保存图片路径
        //从商品详情表中查询图片列表
        // [{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
        String itemImages = goodsVo.getGoodsDesc().getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        //判断图片列表是否为null或者空
        if (null != images && images.size() >0 ){
            item.setImage(String.valueOf(images.get(0).get("url")));
        }
        //设置三级分类id
        item.setCategoryid(goodsVo.getGoods().getCategory3Id());
        //设置三级分类名称
        item.setCategory(itemCatDao.selectByPrimaryKey(goodsVo.getGoods().getCategory3Id()).getName());
        //设置创建时间
        item.setCreateTime(new Date());
        //设置更新时间
        item.setUpdateTime(new Date());
        //设置商品id（外键）
        item.setGoodsId(goodsVo.getGoods().getId());
        //设置商家id,直接从controller层获取
        item.setSeller(goodsVo.getGoods().getSellerId());
        //设置商家名称
        item.setSeller(sellerDao.selectByPrimaryKey(goodsVo.getGoods().getSellerId()).getNickName());
        //设置品牌名称
        Brand brand = brandDao.selectByPrimaryKey(goodsVo.getGoods().getBrandId());
        item.setBrand(brand.getName());
    }

    //根据id查询包装类对象，进行数据回显
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo vo = new GoodsVo();
        //设置商品表
        vo.setGoods(goodsDao.selectByPrimaryKey(id));
        //设置商品详情表
        vo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(id));
        //设置库存表
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(vo.getGoods().getId());
        vo.setItemList(itemDao.selectByExample(itemQuery));
        return vo;
    }

    //保存修改
    @Override
    public void update(GoodsVo vo) {
        //插入sku列表数据
        //修改商品


            goodsDao.updateByPrimaryKeySelective(vo.getGoods());
            goodsDescDao.updateByPrimaryKeySelective(vo.getGoodsDesc());
            //先删除 再添加
            ItemQuery itemQuery = new ItemQuery();
            itemQuery.createCriteria().andGoodsIdEqualTo(vo.getGoods().getId());
            itemDao.deleteByExample(itemQuery);
            //保存SKu表  库存
            //判断是否启用规格
            if("1".equals(vo.getGoods().getIsEnableSpec())){
                //启用
                List<Item> itemList = vo.getItemList();
                for (Item item : itemList) {
                    Map<String, Object> map = JSON.parseObject(item.getSpec());
                    //标题
                    String title = vo.getGoods().getGoodsName();
                    Set<Map.Entry<String, Object>> entrySet = map.entrySet();
                    for (Map.Entry<String, Object> entry : entrySet) {
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);
                    //创建Item对象
                    setItemValues(vo,item);
                    //保存item表
                    itemDao.insertSelective(item);
                }
            }else{
                //不启用
                Item item = new Item();
                //标题
                item.setTitle(vo.getGoods().getGoodsName());
                //价格
                item.setPrice(vo.getGoods().getPrice());
                //库存
                item.setNum(99999);
                //规格
                item.setSpec("{}");
                //状态 启用
                item.setStatus("1");
                //默认
                item.setIsDefault("1");
                //设置Item的属性
                setItemValues(vo, item);
                //保存item表
                itemDao.insertSelective(item);

            }
        }

        //开始审核
        @Override
        public void updateStatus(Long[] ids, String status) {
           //创建商品
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            //判断商品id
            if (null != ids){
                for (Long id : ids) {
                    //更新
                    goods.setId(id);
                    goodsDao.updateByPrimaryKeySelective(goods);
                    //只有在审核通过的时候才会执行下面代码
                    if ("1".equals(status)){
                        /*
                            由于涉及到操作mysql、索引库和磁盘
                            所以必须使用分布式事务管理ActiveMQ
                         */
                        //发消息
                        jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                            @Override
                            public Message createMessage(Session session) throws JMSException {
                                return session.createTextMessage(String.valueOf(id));
                            }
                        });
                    }
                }
            }
        }

    //逻辑删除，变更is_delete属性的状态（null为不删除， 1为已删除）
    @Override
    public void delete(Long[] ids) {
        if (null != ids){
            Goods goods = new Goods();
            goods.setIsDelete("1");     //逻辑删除
            for (Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);

                //逻辑删除商品时，需要发消息提醒solr库删除索引
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(String.valueOf(id));
                    }
                });
            }
        }
    }

    //创建Item对象
        public void setItemValues(GoodsVo vo,Item item){
            //图片保存第一张
            List<Map> list = JSON.parseArray(vo.getGoodsDesc().getItemImages(), Map.class);
            item.setImage((String) list.get(0).get("url"));
            //商品分类Id  保存第三级商品分类ID
            item.setCategoryid(vo.getGoods().getCategory3Id());
            //商品分类名称
            ItemCat itemCat = itemCatDao.selectByPrimaryKey(vo.getGoods().getCategory3Id());
            item.setCategory(itemCat.getName());
            //添加时间
            item.setCreateTime(new Date());
            //修改时间
            item.setUpdateTime(new Date());
            //商品ID
            item.setGoodsId(vo.getGoods().getId());
            //当前登陆的用户ID
            item.setSellerId(vo.getGoods().getSellerId());
            //当前登陆的用户名
            Seller seller = sellerDao.selectByPrimaryKey(vo.getGoods().getSellerId());
            item.setSeller(seller.getName());
            //品牌名称
            Brand brand = brandDao.selectByPrimaryKey(vo.getGoods().getBrandId());
            item.setBrand(brand.getName());
        }


}
