package cn.itcast.core.controller;


import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 品牌处理器
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;


    /**
     * 查询所有
     * @return
     */
    @RequestMapping("/findAll")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

    /**
     * 使用分页助手，查询分页信息,有添加条件
     */
    @RequestMapping("/search")
    public PageResult search(Integer pageNum, Integer pageSize, @RequestBody Brand brand){
        return brandService.search(pageNum,pageSize, brand);
    }

    /**
     * 使用分页助手，查询分页信息，无添加条件
     */
    @RequestMapping("/findPage")
    public PageResult getByPage(Integer pageNum, Integer pageSize){
        return brandService.getByPage(pageNum,pageSize);
    }

    /**
     * 添加品牌
     */
    @RequestMapping("/add")
    public Result save(@RequestBody Brand brand){
        try {
            brandService.save(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    /**
     * 根据id，回显数据信息
     */
    @RequestMapping("/findOne")
    public Brand showOldData(Long id){
        Brand brand = brandService.findById(id);
        return brand;
    }

    /**
     * 根据id更新品牌信息
     */

    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改成功");
        }
    }

    /**
     * 删除单个或多个
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){

        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    /**
     * 模板管理中关联品牌列表
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        /*
            之所以不用findAll（）查询所有的原因，是因为我们的下拉框的对象只需要两个字段，
            而通过findAll查询的三个字段，不方便处理
         */
        List<Map> map = brandService.selectOptionList();
        return map;
    }





}

