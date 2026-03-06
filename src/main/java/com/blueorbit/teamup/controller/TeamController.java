package com.blueorbit.teamup.controller;

import com.blueorbit.teamup.auth.AuthHelper;
import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.domain.Comment;
import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.domain.Team;
import com.blueorbit.teamup.service.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-02
 */
@RestController
@RequestMapping("/teams")
public class TeamController {
    private final ITeamService teamService;
    private final IInfoService infoService;
    private final ICommentService commentService;
    private final IApplicationService applicationService;
    private final IWorkflowService workflowService;

    public TeamController(ITeamService teamService,
                          IInfoService infoService,
                          ICommentService commentService,
                          IApplicationService applicationService,
                          IWorkflowService workflowService) {
        this.teamService = teamService;
        this.infoService = infoService;
        this.commentService = commentService;
        this.applicationService = applicationService;
        this.workflowService = workflowService;
    }

    @PostMapping
    @CrossOrigin
    public Result save(@RequestBody TeamInfo teamInfo, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        if (teamInfo == null || teamInfo.getTeam() == null || teamInfo.getInfo() == null) {
            return new Result(Code.PARAM_ERR, null, Msg.PARAM_INVALID);
        }
        if (!Objects.equals(currentUserId, teamInfo.getTeam().getCreatorId())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        boolean flag = workflowService.createTeam(teamInfo);
        return new Result(flag ? Code.SAVE_TEAM_OK : Code.SAVE_TEAM_ERR,flag);
    }

    @PutMapping
    @CrossOrigin
    public Result update(@RequestBody TeamInfo teamInfo, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        if (teamInfo == null || teamInfo.getTeam() == null || teamInfo.getTeam().getId() == null) {
            return new Result(Code.PARAM_ERR, null, Msg.PARAM_INVALID);
        }
        Team dbTeam = teamService.getById(teamInfo.getTeam().getId());
        if (dbTeam == null) {
            return new Result(Code.GET_TEAM_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        if (!Objects.equals(currentUserId, dbTeam.getCreatorId())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        teamInfo.getTeam().setCreatorId(dbTeam.getCreatorId());
        teamInfo.getTeam().setTeammates(dbTeam.getTeammates());
        teamInfo.getTeam().setInfoId(dbTeam.getInfoId());
        boolean flag = workflowService.updateTeam(teamInfo);
        return new Result(flag ? Code.UPDATE_TEAM_OK : Code.UPDATE_TEAM_ERR,flag);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public Result getById(@PathVariable Long id){
        Team team = teamService.getById(id);
        if (team == null) {
            return new Result(Code.GET_TEAM_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        Info info = infoService.getByTeamId(id);
        TeamInfo teamInfo = new TeamInfo();
        teamInfo.setTeam(team);
        teamInfo.setInfo(info);
        teamInfo.setCommentList(commentService.getByTeamId(id));
        teamInfo.setApplicationList(applicationService.getByTeamId(id));
        return new Result(Code.GET_TEAM_OK,teamInfo,"");
    }

    @DeleteMapping("/{id}")
    @CrossOrigin
    public Result deleteById(@PathVariable Long id, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        Team dbTeam = teamService.getById(id);
        if (dbTeam == null) {
            return new Result(Code.DELETE_TEAM_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        if (!Objects.equals(currentUserId, dbTeam.getCreatorId())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        boolean flag = teamService.delete(id);
        return new Result(flag ? Code.DELETE_TEAM_OK : Code.DELETE_TEAM_ERR,flag);
    }

    @GetMapping
    @CrossOrigin
    public Result getAll(){
        List<Team> teamList = teamService.getAll();
        Integer code = null != teamList ? Code.GET_ALL_TEAM_OK : Code.GET_ALL_TEAM_ERR;
        String msg = null != teamList ? "" : "No team list";
        List<Info> allInfo = infoService.getAll();
        Map<Long, Info> infoByTeamId = allInfo.stream()
                .filter(info -> info.getTeamId() != null)
                .collect(Collectors.toMap(Info::getTeamId, info -> info, (left, right) -> left));
        List<Comment> allComments = commentService.getAll();
        Map<Long, List<Comment>> commentsByTeamId = allComments.stream()
                .collect(Collectors.groupingBy(Comment::getTeamId));
        List<Application> allApplications = applicationService.getAll();
        Map<Long, List<Application>> applicationsByTeamId = allApplications.stream()
                .collect(Collectors.groupingBy(Application::getTid));
        List<TeamInfo> teamInfoList = new ArrayList<>();
        for (Team team:teamList) {
            TeamInfo tmp = new TeamInfo();
            tmp.setTeam(team);
            tmp.setInfo(infoByTeamId.get(team.getId()));
            tmp.setCommentList(commentsByTeamId.getOrDefault(team.getId(), Collections.emptyList()));
            tmp.setApplicationList(applicationsByTeamId.getOrDefault(team.getId(), Collections.emptyList()));
            teamInfoList.add(tmp);
        }
        return new Result(code,teamInfoList,msg);
    }
}

