package cn.itcast.core.service;

import cn.itcast.core.dao.address.AddressDao;
import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.AddressQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AddressDao addressDao;
    //根据当前用户名，查询收货地址列表
    @Override
    public List<Address> findListByLoginUser(String name) {
        AddressQuery addressQuery = new AddressQuery();
        AddressQuery.Criteria criteria = addressQuery.createCriteria();
        criteria.andUserIdEqualTo(name);
        return addressDao.selectByExample(addressQuery);
    }
}
