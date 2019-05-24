package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.GoodsVo;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品管理
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {


    @Reference
    GoodsService goodsService;

    /**
     * 查询
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){
        //获取商家Id
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(name);
        return goodsService.search(page, rows, goods);
    }

    /**
     * 添加商品
     */
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsVo goodsVo){
        try {
            //获取上一次记录的id(商家id，也就是商家名称)
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //设置商家id
            goodsVo.getGoods().setSellerId(name);
            goodsService.add(goodsVo);
            return new Result(true,"添加商品成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加商品失败");
        }
    }

    /**
     * 根据id，查询包装类对象，进行数据回显
     */
    @RequestMapping("/findOne")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }

    /**
     * 保存修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsVo goodsVo){
        try {
            goodsService.update(goodsVo);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }



}

