package com.blueorbit.teamup.service.impl;

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
        applicationDao.insert(application);
        return true;
    }

    @Override
    public boolean update(Application application) {
        applicationDao.updateById(application);
        return true;
    }

    @Override
    public boolean delete(Long id) {
        applicationDao.deleteById(id);
        return true;
    }

    @Override
    public Application getById(Long id) {
        return applicationDao.selectById(id);
    }

    @Override
    public List<Application> getAll() {
        return applicationDao.selectList(null);
    }
}
