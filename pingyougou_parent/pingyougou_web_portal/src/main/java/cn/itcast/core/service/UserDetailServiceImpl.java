package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

/**
 * 认证工作：查询用户名和密码
 */
public class UserDetailServiceImpl implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            //审核通过的用户
            ArrayList<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
            list.add(new SimpleGrantedAuthority("ROLE_USER"));
            return new User(username, "", list);
    }
}
