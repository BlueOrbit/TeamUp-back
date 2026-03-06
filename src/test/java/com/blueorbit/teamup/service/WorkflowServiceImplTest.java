package com.blueorbit.teamup.service;

import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.domain.Team;
import com.blueorbit.teamup.domain.User;
import com.blueorbit.teamup.service.impl.WorkflowServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowServiceImplTest {

    @Test
    void acceptApplicationShouldFailWhenTeamIsFull() {
        ITeamService teamService = mock(ITeamService.class);
        IInfoService infoService = mock(IInfoService.class);
        IUserService userService = mock(IUserService.class);
        IApplicationService applicationService = mock(IApplicationService.class);

        WorkflowServiceImpl workflowService = new WorkflowServiceImpl(teamService, infoService, userService, applicationService);

        Application current = new Application();
        current.setId(10L);
        current.setUid(1L);
        current.setTid(2L);
        current.setState(Application.STATE.WAIT.ordinal());

        Application update = new Application();
        update.setId(10L);
        update.setState(Application.STATE.ACCEPT.ordinal());

        Team team = new Team();
        team.setId(2L);
        team.setTeammates("3;4;");

        User applicant = new User();
        applicant.setId(1L);

        Info info = new Info();
        info.setTeamId(2L);
        info.setNumberLimit(2);

        when(applicationService.getById(10L)).thenReturn(current);
        when(teamService.getById(2L)).thenReturn(team);
        when(userService.getById(1L)).thenReturn(applicant);
        when(infoService.getByTeamId(2L)).thenReturn(info);

        assertThrows(IllegalStateException.class, () -> workflowService.updateApplication(update));
    }
}
