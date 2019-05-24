package cn.itcast.core.service;

import cn.itcast.core.pojo.specification.Specification;
import entity.PageResult;
import entity.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    //查询分页信息
    PageResult search(Integer page, Integer rows, Specification specification);

    //添加保存
    void add(SpecificationVo specificationVo);

    //根据id，回显数据
    SpecificationVo findOne(Long id);

    //规格下拉框关联查询列表
    List<Map> selectOptionList();
}
