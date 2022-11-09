package com.blueorbit.teamup;

import com.blueorbit.teamup.dao.InfoDao;
import com.blueorbit.teamup.dao.TeamDao;
import com.blueorbit.teamup.dao.UserDao;
import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.domain.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.blueorbit.teamup.domain.User;

@SpringBootTest
class TeamUpApplicationTests {

	@Autowired
	private UserDao userDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private InfoDao infoDao;


	void contextLoads() {
		System.out.println("222");

	}

	void testInsert(){
		User user = new User();
		user.setEmail("ooo");
		user.setPassword("84fd");
		user.setName("852");
		user.setTeams("");
		userDao.insert(user);
	}

	void testTeamInsert(){
		Team team = new Team();
		Info info = new Info();
		info.setContent("introduction for team");
		info.setCourse("course1");
		info.setNumberLimit(8);
		infoDao.insert(info);
		System.out.println(info.getId());

//		team.setCommentList("");
//		team.setName("");
//		team.setTeammates("");
//		team.setCreator_id(1L);
//		team.setInfoId(1L);
//		teamDao.insert(team);
	}

}
