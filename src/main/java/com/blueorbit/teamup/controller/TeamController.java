package com.blueorbit.teamup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueorbit.teamup.dao.InfoDao;
import com.blueorbit.teamup.dao.TeamDao;
import com.blueorbit.teamup.dao.UserDao;
import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.domain.Team;
import com.blueorbit.teamup.domain.User;
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
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private TeamDao teamDao;

    @Autowired
    private InfoDao infoDao;

    @Autowired
    private UserDao userDao;

    @GetMapping("/create")
    public String createTeam(@RequestParam("creator_id") Long creator_id,
                            @RequestParam("team_name") String team_name,
                            @RequestParam("info_content") String info_content,
                            @RequestParam("number_limit") int number_limit){

        Info info = new Info();
        info.setNumber_limit(number_limit);
        info.setCourse("course1");
        info.setContent(info_content);
        infoDao.insert(info);

        Team team = new Team();
        team.setCreator_id(creator_id);
        team.setInfo_id(info.getId());
        team.setName(team_name);
        team.setTeammates(creator_id.toString());
        team.setCommentlist("");
        teamDao.insert(team);
        System.out.println(team.getId());

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getId,creator_id);
        User user = userDao.selectOne(userLambdaQueryWrapper);
        user.joinTeam(team.getId());
        userDao.updateById(user);

        return "Team id:" + team.getId().toString();
//        http://localhost:8080/team/create?creator_id=7&team_name=Team77&info_content=lessthan32&number_limit=6

    }

    @GetMapping("/showAll")
    public List<Team> showAll(){
        return teamDao.selectList(null);
    }

}

