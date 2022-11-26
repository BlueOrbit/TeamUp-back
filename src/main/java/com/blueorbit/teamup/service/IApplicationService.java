package com.blueorbit.teamup.service;

import com.blueorbit.teamup.domain.Application;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface IApplicationService {
    public boolean save(Application application);

    public boolean update(Application application);

    public boolean delete(Long id);

    public Application getById(Long id);

    public List<Application> getAll();
}
