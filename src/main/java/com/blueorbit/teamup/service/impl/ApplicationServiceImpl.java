package com.blueorbit.teamup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueorbit.teamup.dao.ApplicationDao;
import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.service.IApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-25
 */
@Service
public class ApplicationServiceImpl implements IApplicationService {
    @Autowired
    private ApplicationDao applicationDao;

    @Override
    public boolean save(Application application) {
        return applicationDao.insert(application) > 0;
    }

    @Override
    public boolean update(Application application) {
        return applicationDao.updateById(application) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return applicationDao.deleteById(id) > 0;
    }

    @Override
    public Application getById(Long id) {
        return applicationDao.selectById(id);
    }

    @Override
    public List<Application> getByUserId(Long uid) {
        LambdaQueryWrapper<Application> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Application::getUid,uid);
        return applicationDao.selectList(lqw);
    }

    @Override
    public List<Application> getByTeamId(Long tid) {
        LambdaQueryWrapper<Application> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Application::getTid,tid);
        return applicationDao.selectList(lqw);
    }

    @Override
    public List<Application> getAll() {
        return applicationDao.selectList(null);
    }
}
