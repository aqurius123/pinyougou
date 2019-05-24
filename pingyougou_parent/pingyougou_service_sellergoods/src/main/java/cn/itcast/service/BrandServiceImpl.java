package cn.itcast.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
    @Autowired
    BrandDao brandDao;
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    //查询分页信息,封装PageResult结果集，有条件
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
       //开启分页
        PageHelper.startPage(pageNum,pageSize);
        //查询所有
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();

        //判断品牌名称
        if (null != brand.getName() && !"".equals(brand.getName())){
            criteria.andNameLike("%"+brand.getName()+"%");
        }
        //判断首字母

        if(null != brand.getFirstChar() && !"".equals(brand.getFirstChar())){
            criteria.andFirstCharEqualTo(brand.getFirstChar());
        }
        List<Brand> brandList = brandDao.selectByExample(brandQuery);
        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);
        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }



    //查询分页信息,封装PageResult结果集，无条件
    @Override
    public PageResult getByPage(Integer pageNum, Integer pageSize) {
        //开启分页
        PageHelper.startPage(pageNum,pageSize);
        //查询所有
        List<Brand> brandList = brandDao.selectByExample(null);
        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);
        /**
         * 下面这一行代码不是很熟悉，再理解
         */
        PageResult pageResult = new PageResult(pageInfo.getTotal(),pageInfo.getList());
        return pageResult;
    }

    //添加品牌信息
    @Override
    public void save(Brand brand) {
        brandDao.insertSelective(brand);
    }

    //根据id，回显数据
    @Override
    public Brand findById(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }


    //根据id修改
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    //删除单个或多个
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            brandDao.deleteByPrimaryKey(id);
        }
    }


    //查询关联下拉框
    @Override
    public List<Map> selectOptionList() {
        List<Map> map = brandDao.selectOptionList();
        return map;
    }



}
