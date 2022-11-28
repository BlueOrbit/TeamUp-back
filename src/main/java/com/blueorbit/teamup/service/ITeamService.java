package com.blueorbit.teamup.service;

import com.blueorbit.teamup.domain.Team;
import com.blueorbit.teamup.domain.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-08
 */
@Transactional
public interface ITeamService{
    public boolean save(Team team);

    public boolean update(Team team);

    public boolean delete(Long id);

    public Team getById(Long id);

    public List<Team> getAll();

    public boolean addUser(Long tid, Long uid);
}
