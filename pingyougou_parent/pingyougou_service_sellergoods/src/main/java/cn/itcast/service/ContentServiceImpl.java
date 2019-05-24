package cn.itcast.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public List<Content> findAll() {
        List<Content> list = contentDao.selectByExample(null);
        return list;
    }

    @Override
    public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(Content content) {
        contentDao.insertSelective(content);
        /**
         * 清空缓存
         */
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
    }

    @Override
    public void edit(Content content) {
        //查询原先的缓存
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());
        contentDao.updateByPrimaryKeySelective(content);

        /**
         * 考虑到运营商可能修改的时候，将广告的类型也修改，
         * 所以要清空原先的缓存和新分类的缓存
         */
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        if (oldContent.getCategoryId() != content.getCategoryId()){
            //清空原缓存
            redisTemplate.boundHashOps("content").delete(oldContent.getCategoryId());
        }
    }

    @Override
    public Content findOne(Long id) {
        Content content = contentDao.selectByPrimaryKey(id);
        return content;
    }

    @Override
    public void delAll(Long[] ids) {
        if(ids != null){
            for(Long id : ids){
                Content content = contentDao.selectByPrimaryKey(id);
                contentDao.deleteByPrimaryKey(id);
                /**
                 * 清空缓存
                 */
                //根据分类id清除缓存
                redisTemplate.boundHashOps("content").delete(content.getCategoryId());
            }
        }
    }

    //根据id查询轮播图
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        //根据广告分类id先从缓存获取轮播图
        List<Content> contentList = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
        //没有，查数据库,存入缓存一份
        if (null == contentList || contentList.size() == 0){
            ContentQuery contentQuery = new ContentQuery();
            ContentQuery.Criteria criteria = contentQuery.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
            contentList = contentDao.selectByExample(contentQuery);
            //存入缓存
            redisTemplate.boundHashOps("content").put(categoryId, contentList);
            //设置有效期
            redisTemplate.boundHashOps("content").expire(24, TimeUnit.HOURS);
        }
        //返回
        return contentList;
    }

}