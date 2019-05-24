package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    /**
     * 通过父id查询分类信息
     */
    @Reference
    private ItemCatService itemCatService;
    @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId){
        return itemCatService.findByParentId(parentId);
    }

    /**
     * 添加
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ItemCat itemCat){
        try {
            itemCatService.add(itemCat);
            return new Result(true, "添加 成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加 失败");
        }

    }

    /**
     * 根据id查询，并数据回显
     */
    @RequestMapping("/findOne")
    public ItemCat findOne(Long id){
        return itemCatService.findOne(id);

    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ItemCat itemCat){
        try {
            itemCatService.update(itemCat);
            return new Result(true, "修改 成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改 失败");
        }

    }

    /**
     * 删除单个或多个
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            itemCatService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询所有
     */
    @RequestMapping("/findAll")
    public List<ItemCat> findAll(){
        return itemCatService.findAll();
    }



}
