package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 模板管理
 */
@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {


    @Reference
    TypeTemplateService typeTemplateService;

    /**
     * 条件查询
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows , @RequestBody TypeTemplate typeTemplate){
        return typeTemplateService.search(page, rows, typeTemplate);
    }

    /**
     * 添加模板
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.add(typeTemplate);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }

    /**
     * 删除单个或多个
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            typeTemplateService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 根据id，查找一个，并数据回显
     */
    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id){
        return typeTemplateService.findOne(id);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.update(typeTemplate);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 根据模板id查询规格列表
     */
    @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(Long id){
        return typeTemplateService.findBySpecList(id);
    }

}
