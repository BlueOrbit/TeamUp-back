package com.blueorbit.teamup.controller;

import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.domain.Team;
import com.blueorbit.teamup.domain.User;
import com.blueorbit.teamup.service.ICommentService;
import com.blueorbit.teamup.service.IInfoService;
import com.blueorbit.teamup.service.ITeamService;
import com.blueorbit.teamup.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Autowired
    private ITeamService teamService;
    @Autowired
    private IInfoService infoService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ICommentService commentService;
    @PostMapping
    public Result save(@RequestBody TeamInfo teamInfo){
        boolean flag_team = teamService.save(teamInfo.team);
//        System.out.println(teamInfo.team.getId());
        teamInfo.team.setInfoId(teamInfo.team.getId());
        teamInfo.team.setTeammates(teamInfo.team.getCreatorId().toString());
        teamService.update(teamInfo.team);

        teamInfo.info.setTeamId(teamInfo.team.getId());
        boolean flag_info = infoService.save(teamInfo.info);

        User ct_user = userService.getById(teamInfo.team.getCreatorId());
        ct_user.setTeams(ct_user.getTeams()+","+teamInfo.team.getId());
        boolean flag_user = userService.update(ct_user);
        boolean flag = flag_team & flag_info & flag_user;
        return new Result(flag ? Code.SAVE_TEAM_OK : Code.SAVE_TEAM_ERR,flag);
    }
    @PutMapping
    public Result update(@RequestBody TeamInfo teamInfo){
        boolean flag_team = teamService.update(teamInfo.team);
        teamInfo.info.setId(teamInfo.team.getId());
        boolean flag_info = infoService.update(teamInfo.info);
        boolean flag = flag_team & flag_info;
        return new Result(flag ? Code.UPDATE_TEAM_OK : Code.UPDATE_TEAM_ERR,flag);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        Team team = teamService.getById(id);
        Integer code = null != team ? Code.GET_TEAM_OK : Code.GET_TEAM_ERR;
        String msg = null != team ? "" : "No team for this id";
        System.out.println(team);
        Info info = infoService.getByTeamId(id);
        System.out.println(info);
        TeamInfo teamInfo = new TeamInfo();
        teamInfo.setTeam(team);
        teamInfo.setInfo(info);
        teamInfo.setCommentList(commentService.getByTeamId(id));
        System.out.println(teamInfo);
        return new Result(code,teamInfo,msg);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        boolean flag = teamService.delete(id);
        return new Result(flag ? Code.DELETE_TEAM_OK : Code.DELETE_TEAM_ERR,flag);
    }

    @GetMapping
    public Result getAll(){
        List<Team> teamList = teamService.getAll();
        Integer code = null != teamList ? Code.GET_ALL_TEAM_OK : Code.GET_ALL_TEAM_ERR;
        String msg = null != teamList ? "" : "No team list";
        return new Result(code,teamList,msg);
    }
    
    

}

