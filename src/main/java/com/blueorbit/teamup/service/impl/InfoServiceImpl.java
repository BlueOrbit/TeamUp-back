package com.blueorbit.teamup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueorbit.teamup.dao.InfoDao;
import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.service.IInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-08
 */
@Service
public class InfoServiceImpl implements IInfoService {
    @Autowired
    private InfoDao infoDao;

    @Override
    public boolean save(Info info) {
        infoDao.insert(info);
        return true;
    }

    @Override
    public boolean update(Info info) {
        infoDao.updateById(info);
        return true;
    }

    @Override
    public boolean updateByTeamId(Info info,Long tid) {
        LambdaQueryWrapper<Info> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Info::getTeamId,tid);
        infoDao.update(info,lqw);
        return true;
    }

    @Override
    public boolean delete(Long id) {
        infoDao.deleteById(id);
        return true;
    }

    @Override
    public Info getById(Long id) {
        return infoDao.selectById(id);
    }

    @Override
    public Info getByTeamId(Long id) {
        LambdaQueryWrapper<Info> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Info::getTeamId,id);
        return infoDao.selectOne(lqw);
    }

    @Override
    public List<Info> getByContent(String str) {
        LambdaQueryWrapper<Info> lqw = new LambdaQueryWrapper<>();
        lqw.like(Info::getContent,str);
        return infoDao.selectList(lqw);
    }

    @Override
    public List<Info> getAll() {
        return infoDao.selectList(null);
    }
}
