package cn.itcast.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {
    @Autowired
    ItemCatDao itemCatDao;
    @Autowired
    RedisTemplate redisTemplate;
    //根据父id查询分类
    @Override
    public List<ItemCat> findByParentId(Long parentId) {
//        ItemCatQuery itemCatQuery = new ItemCatQuery();
//        itemCatQuery.createCriteria().andParentIdEqualTo(parentId);
        //（1）当用户进入运营商后台的商品分类页面时，将商品分类数据放入缓存（Hash）。以分类名称作为key ,以模板ID作为值
            //每次执行查询的时候，一次性读取缓存进行存储（因为每次进行增删改都要进行此操作）
        List<ItemCat> list = findAll();
            for (ItemCat item : list) {
                //获取模板id
                redisTemplate.boundHashOps("itemCat").put(item.getName(), item.getTypeId());
            }
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        itemCatQuery.createCriteria().andParentIdEqualTo(parentId);
        return itemCatDao.selectByExample(itemCatQuery);
    }

    //添加
    @Override
    public void add(ItemCat itemCat) {
        itemCatDao.insertSelective(itemCat);
    }

    //根据id查询单个
    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    //修改
    @Override
    public void update(ItemCat itemCat) {
        itemCatDao.updateByPrimaryKeySelective(itemCat);
    }

    //删除单个或多个
    @Override
    public void delete(Long[] ids) {
        if (null != ids){
            for (Long id : ids) {
                itemCatDao.deleteByPrimaryKey(id);
            }
        }
    }

    //查询所有
    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }
}
