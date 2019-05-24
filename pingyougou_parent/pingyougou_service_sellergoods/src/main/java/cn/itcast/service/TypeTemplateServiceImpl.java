package cn.itcast.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Autowired
    TypeTemplateDao typeTemplateDao;
    @Autowired
    SpecificationOptionDao specificationOptionDao;
    @Autowired
    RedisTemplate redisTemplate;
    //条件查询
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {

        /*
            缓存品牌列表和规格列表
         */
        saveToRedis();
        //开启分页助手
        PageHelper.startPage(page, rows);
        //查询所有
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria();
        //判断条件是否为空
        if (null != typeTemplate.getName() && !"".equals(typeTemplate.getName())) {
            //拼接条件
            criteria.andNameLike("%" + typeTemplate.getName() + "%");
        }
        List<TypeTemplate> typeTemplateList = typeTemplateDao.selectByExample(typeTemplateQuery);
        PageInfo<TypeTemplate> pageInfo = new PageInfo<>(typeTemplateList);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /*
        根据模板，将品牌和规格存入redis
        （2）当用户进入运营商后台的模板管理页面时，分别将品牌数据和规格数据放入缓存（Hash）。
             以模板ID作为key,以品牌列表和规格列表作为值。
     */
    public void saveToRedis(){

        //获取模板列表
        List<TypeTemplate> typeTemplateList = typeTemplateDao.selectByExample(null);
        for (TypeTemplate typeTemplate : typeTemplateList) {
            //获取品牌列表,因为品牌列表内部都是多个键值对对象，所有泛型为Map
            // [{"id":1,"text":"联想"},{"id":3,"text":"三星"}]
            String brandIds = typeTemplate.getBrandIds();
            List<Map> brandList = JSON.parseArray(brandIds, Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);
            //获取规格列表[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
//            String specIds = typeTemplate.getSpecIds();
//            List<Specification> specificationList = JSON.parseArray(specIds, Specification.class);
            List<Map> specList = findBySpecList(typeTemplate.getId());
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }
    }

    //添加
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids != null){
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }
    }

    //查询单个
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    //修改
    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    //根据模板id查询规格列表
    @Override
    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        List<Map> specListMap = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
        //查询规格选项列表
        for (Map map : specListMap) {
            //根据外键查询规格选项表
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
            SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
            criteria.andSpecIdEqualTo(Long.valueOf(((Integer)map.get("id"))));
            List<SpecificationOption> options = specificationOptionDao.selectByExample(specificationOptionQuery);
            map.put("options", options);
        }
        return specListMap;

    }
}