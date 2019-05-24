package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    //根据父id查询分类
    List<ItemCat> findByParentId(Long parentId);

    //添加
    void add(ItemCat itemCat);

    //根据id查询单个
    ItemCat findOne(Long id);

    //修改
    void update(ItemCat itemCat);

    //删除
    void delete(Long[] ids);

    List<ItemCat> findAll();
}
