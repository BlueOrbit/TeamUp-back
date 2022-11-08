package com.blueorbit.teamup.service;

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
public interface IUserService {
    public boolean save(User user);

    public boolean update(User user);

    public boolean delete(Long id);

    public User getById(Long id);

    public List<User> getAll();
}
