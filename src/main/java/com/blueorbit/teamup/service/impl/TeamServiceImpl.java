package com.blueorbit.teamup.service.impl;

import com.blueorbit.teamup.dao.TeamDao;
import com.blueorbit.teamup.domain.Team;
import com.blueorbit.teamup.service.ITeamService;
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
public class TeamServiceImpl  implements ITeamService {
    @Autowired
    private TeamDao teamDao;

    @Override
    public boolean save(Team team) {
        return teamDao.insert(team) > 0;
    }

    @Override
    public boolean update(Team team) {
        return teamDao.updateById(team) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return teamDao.deleteById(id) > 0;
    }

    @Override
    public Team getById(Long id) {
        return teamDao.selectById(id);
    }

    @Override
    public List<Team> getAll() {
        return teamDao.selectList(null);
    }

}
