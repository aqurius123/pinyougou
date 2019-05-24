package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {
    //添加
    void add(Seller seller);

    //用户认证
    Seller findOne(String username);
}
