package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.service.IApplicationService;
import com.blueorbit.teamup.service.ICommentService;
import com.blueorbit.teamup.service.IInfoService;
import com.blueorbit.teamup.service.ITeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
@CrossOrigin
@RequestMapping("/info")
public class InfoController {
    @Autowired
    private IInfoService infoService;
    @Autowired
    private ITeamService teamService;
    @Autowired
    private ICommentService commentService;

    @Autowired
    private IApplicationService applicationService;


    @GetMapping("/getinfo/{id}")
    @CrossOrigin
    public Info getInfo(@PathVariable Long id){
        return infoService.getById(id);
    }

    @PostMapping("/search")
    @CrossOrigin
    public Result searchInfoContent(@RequestBody Info info){
        List<Info> infos= infoService.getByContent(info.getContent());
        List<TeamInfo> teamInfoList = new ArrayList<>();
        for (Info i:infos
        ) {
            TeamInfo tmp = new TeamInfo();
            Long tid = i.getTeamId();
            tmp.setTeam(teamService.getById(tid));
            tmp.setInfo(infoService.getById(tid));
            tmp.setCommentList(commentService.getByTeamId(tid));
            tmp.setApplicationList(applicationService.getByTeamId(tid));
            teamInfoList.add(tmp);
        }
        return new Result(Code.SEARCH_INFO_OK,teamInfoList,null);
    }

}

