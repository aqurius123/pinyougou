package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Brand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    /**
     * 查询所有品牌
     */
    List<Brand> findAll();

    //查询分页信息
    PageResult getByPage(Integer pageNum, Integer pageSize);

    //添加品牌信息
    void save(Brand brand);

    //根据id，回显数据信息
    Brand findById(Long id);

    //根据id修改数据信息
    void update(Brand brand);

    //删除单个或多个
    void delete(Long[] ids);


    //根据条件模糊查询
    PageResult search(Integer pageNum, Integer pageSize, Brand brand);

    //查询品牌关联下拉框
    List<Map> selectOptionList();
}
