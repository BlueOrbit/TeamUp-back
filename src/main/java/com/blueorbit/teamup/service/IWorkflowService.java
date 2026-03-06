package com.blueorbit.teamup.service;

import com.blueorbit.teamup.controller.TeamInfo;
import com.blueorbit.teamup.domain.Application;

public interface IWorkflowService {
    boolean createTeam(TeamInfo teamInfo);

    boolean updateTeam(TeamInfo teamInfo);

    boolean updateApplication(Application application);
}
