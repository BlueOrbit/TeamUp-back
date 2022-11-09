package com.blueorbit.teamup.service;

import com.blueorbit.teamup.domain.Info;
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
public interface IInfoService {
    public boolean save(Info info);

    public boolean update(Info info);

    public boolean delete(Long id);

    public Info getById(Long id);

    public Info getByTeamId(Long id);

    public List<Info> getAll();

}
