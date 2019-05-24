package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Goods;
import entity.GoodsVo;
import entity.PageResult;

public interface GoodsService {
    //按条件， 分页查询
    PageResult search(Integer page, Integer rows, Goods goods);

    void add(GoodsVo goodsVo);

    GoodsVo findOne(Long id);

    void update(GoodsVo goodsVo);

    void updateStatus(Long[] ids, String status);

    void delete(Long[] ids);

}
