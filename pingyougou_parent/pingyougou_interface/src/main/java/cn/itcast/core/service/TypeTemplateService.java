package cn.itcast.core.service;

import cn.itcast.core.pojo.template.TypeTemplate;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService {
    //条件查询
    PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);

    //添加
    void add(TypeTemplate typeTemplate);

    //删除单个或多个
    void delete(Long[] ids);

    //查询单个
    TypeTemplate findOne(Long id);

    //修改
    void update(TypeTemplate typeTemplate);

    List<Map> findBySpecList(Long id);

}
