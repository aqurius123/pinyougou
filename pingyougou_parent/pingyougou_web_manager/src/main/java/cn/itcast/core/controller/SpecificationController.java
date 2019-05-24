package cn.itcast.core.controller;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import entity.SpecificationVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 规格管理
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;
    /**
     * 查询分页信息
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
        return specificationService.search(page, rows, specification);

    }

    /**
     * 保存
     */
    @RequestMapping("/add")
    public Result add(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.add(specificationVo);
            return  new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(true,"添加失败");
        }
    }

    /**
     * 根据id，回显数据
     */
    @RequestMapping("/findOne")
    public SpecificationVo findOne(Long id){
        return  specificationService.findOne(id);
    }

    /**
     * 规格表下拉框关联查询列表
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        List<Map> map = specificationService.selectOptionList();
        return map;
    }
}
