package com.blueorbit.teamup.service.impl;

import com.blueorbit.teamup.controller.Msg;
import com.blueorbit.teamup.controller.TeamInfo;
import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.domain.Team;
import com.blueorbit.teamup.domain.User;
import com.blueorbit.teamup.service.IApplicationService;
import com.blueorbit.teamup.service.IInfoService;
import com.blueorbit.teamup.service.ITeamService;
import com.blueorbit.teamup.service.IUserService;
import com.blueorbit.teamup.service.IWorkflowService;
import com.blueorbit.teamup.util.MembershipUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class WorkflowServiceImpl implements IWorkflowService {
    private final ITeamService teamService;
    private final IInfoService infoService;
    private final IUserService userService;
    private final IApplicationService applicationService;

    public WorkflowServiceImpl(ITeamService teamService,
                               IInfoService infoService,
                               IUserService userService,
                               IApplicationService applicationService) {
        this.teamService = teamService;
        this.infoService = infoService;
        this.userService = userService;
        this.applicationService = applicationService;
    }

    @Override
    @Transactional
    public boolean createTeam(TeamInfo teamInfo) {
        validateTeamInfo(teamInfo);
        Team team = teamInfo.getTeam();
        Info info = teamInfo.getInfo();
        User creator = userService.getById(team.getCreatorId());
        if (creator == null) {
            throw new IllegalStateException(Msg.RESOURCE_NOT_FOUND);
        }
        team.setTeammates(MembershipUtil.toStorageString(Set.of(team.getCreatorId())));
        assertSuccess(teamService.save(team), "Create team failed");

        info.setTeamId(team.getId());
        assertSuccess(infoService.save(info), "Create info failed");

        team.setInfoId(info.getId());
        assertSuccess(teamService.update(team), "Bind info failed");

        creator.setTeams(MembershipUtil.appendUnique(creator.getTeams(), team.getId()));
        assertSuccess(userService.update(creator), "Update creator team list failed");
        return true;
    }

    @Override
    @Transactional
    public boolean updateTeam(TeamInfo teamInfo) {
        validateTeamInfo(teamInfo);
        Team team = teamInfo.getTeam();
        Info info = teamInfo.getInfo();

        assertSuccess(teamService.update(team), "Update team failed");
        Info dbInfo = infoService.getByTeamId(team.getId());
        if (dbInfo == null) {
            info.setTeamId(team.getId());
            assertSuccess(infoService.save(info), "Create info failed");
            team.setInfoId(info.getId());
            assertSuccess(teamService.update(team), "Update team info reference failed");
            return true;
        }
        info.setId(dbInfo.getId());
        assertSuccess(infoService.update(info), "Update info failed");
        return true;
    }

    @Override
    @Transactional
    public boolean updateApplication(Application application) {
        if (application == null || application.getId() == null) {
            throw new IllegalArgumentException(Msg.PARAM_INVALID);
        }
        Application dbApplication = applicationService.getById(application.getId());
        if (dbApplication == null) {
            throw new IllegalStateException(Msg.RESOURCE_NOT_FOUND);
        }
        if (application.getState() == Application.STATE.ACCEPT.ordinal()
                && dbApplication.getState() != Application.STATE.ACCEPT.ordinal()) {
            User user = userService.getById(dbApplication.getUid());
            Team team = teamService.getById(dbApplication.getTid());
            if (user == null || team == null) {
                throw new IllegalStateException(Msg.RESOURCE_NOT_FOUND);
            }
            Set<Long> teammateSet = MembershipUtil.parseIdSet(team.getTeammates());
            if (!teammateSet.contains(user.getId())) {
                Info info = infoService.getByTeamId(team.getId());
                if (info != null && info.getNumberLimit() != null && teammateSet.size() >= info.getNumberLimit()) {
                    throw new IllegalStateException("Team member limit exceeded");
                }
                team.setTeammates(MembershipUtil.appendUnique(team.getTeammates(), user.getId()));
                user.setTeams(MembershipUtil.appendUnique(user.getTeams(), team.getId()));
                assertSuccess(teamService.update(team), "Update team members failed");
                assertSuccess(userService.update(user), "Update user teams failed");
            }
        }
        assertSuccess(applicationService.update(application), "Update application failed");
        return true;
    }

    private void validateTeamInfo(TeamInfo teamInfo) {
        if (teamInfo == null || teamInfo.getTeam() == null || teamInfo.getInfo() == null) {
            throw new IllegalArgumentException(Msg.PARAM_INVALID);
        }
    }

    private void assertSuccess(boolean success, String message) {
        if (!success) {
            throw new IllegalStateException(message);
        }
    }
}
