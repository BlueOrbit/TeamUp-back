package com.blueorbit.teamup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueorbit.teamup.dao.UserDao;
import com.blueorbit.teamup.domain.User;
import com.blueorbit.teamup.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-08
 */
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserDao userDao;

    @Override
    public boolean save(User user) {
        userDao.insert(user);
        return true;
    }

    @Override
    public boolean update(User user) {
        userDao.updateById(user);
        return true;
    }

    @Override
    public boolean delete(Long id) {
        userDao.deleteById(id);
        return true;
    }

    @Override
    public User getById(Long id) {
        return userDao.selectById(id);
    }

    @Override
    public User getByEmail(String email) {
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        lqw.eq(User::getEmail,email);
        return userDao.selectOne(lqw);
    }

    @Override
    public List<User> getAll() {
        return userDao.selectList(null);
    }
}
