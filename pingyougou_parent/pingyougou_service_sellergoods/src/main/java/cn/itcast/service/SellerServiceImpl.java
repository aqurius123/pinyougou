package cn.itcast.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    SellerDao sellerDao;
    //商家入驻
    @Override
    public void add(Seller seller) {
        sellerDao.insertSelective(seller);
    }

    @Override
    public Seller findOne(String username) {
        return sellerDao.selectByPrimaryKey(username);
    }
}
