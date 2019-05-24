package cn.itcast.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.PageResult;
import entity.SpecificationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    SpecificationDao specificationDao;
    @Autowired
    SpecificationOptionDao specificationOptionDao;
    //查询分页信息
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        //开启分页助手
        PageHelper.startPage(page, rows);
        //获取条件容器对象
        SpecificationQuery query = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = query.createCriteria();
        //判断条件是否为空
        if (null  != specification.getSpecName() && !"".equals(specification.getSpecName())){
            //获取条件对象
            criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
//            System.out.println("%"+specification.getSpecName()+"%");
        }
        //查询所有
        List<Specification> specificationList = specificationDao.selectByExample(query);
        PageInfo<Specification> pageInfo = new PageInfo<>(specificationList);
        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }

    //添加保存
    @Override
    public void add(SpecificationVo specificationVo) {
        //将包装类中的数据分别存储到规格表和规格选项表
        //存储规格
        Specification specification = specificationVo.getSpecification();
        specificationDao.insertSelective(specification);
        //存储规格项列表
        //遍历规格项集合
        for (SpecificationOption specificationOption : specificationVo.getSpecificationOptionList()) {
            /*
                规格项表的存储和规格表是一对多的关系，是根据规格表的id来对应存储的，所有需要设置规格表的id
                通过specificationDao.xml配置文件中获取最后一条添加记录的id
             */
            specificationOption.setSpecId(specification.getId());
            specificationOptionDao.insertSelective(specificationOption);
        }
    }

    //根据id，回显数据
    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo specificationVo = new SpecificationVo();
        Specification specification = specificationDao.selectByPrimaryKey(id);
        specificationVo.setSpecification(specification);
        System.out.println(specification.getSpecName());
//        //根据规格表的id，查询规格项列表------下面的操作是有问题的
//        List<SpecificationOption> specificationOptionList = (List<SpecificationOption>) specificationOptionDao.selectByPrimaryKey(specification.getId());
//        System.out.println(specificationOptionList.size());
//        specificationVo.setSpecificationOptionList(specificationOptionList);
        /**
         * 规格项表的查询应该是根据
         */
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(specification.getId());
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
        specificationVo.setSpecificationOptionList(specificationOptionList);
        return specificationVo;
    }

    //规格表下拉框关联查询列表
    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }


}
