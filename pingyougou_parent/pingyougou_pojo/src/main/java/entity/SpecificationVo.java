package entity;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;

import java.io.Serializable;
import java.util.List;

/**
 * 规格包装类
 * 注意：controller层远程调用service层，pojo对象必须实现序列化
 */
public class SpecificationVo implements Serializable{

    private Specification specification;
    private List<SpecificationOption> specificationOptionList;

    public SpecificationVo() {
    }

    public SpecificationVo(Specification specification, List<SpecificationOption> specificationOptionList) {
        this.specification = specification;
        this.specificationOptionList = specificationOptionList;
    }

    public Specification getSpecification() {
        return specification;
    }

    @Override
    public String toString() {
        return "SpecificationVo{" +
                "specification=" + specification +
                ", specificationOptionList=" + specificationOptionList +
                '}';
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public List<SpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<SpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}
