package com.blueorbit.teamup;

import java.util.List;

import com.blueorbit.teamup.dao.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.blueorbit.teamup.domain.User;

@SpringBootTest
class TeamUpApplicationTests {

	@Autowired
	private UserDao userDao;

	@Test
	void contextLoads() {
		System.out.println("222");

	}
	@Test
	void testInsert(){
		User user = new User();
		user.setEmail("888");
		user.setPassword("84fd");
		user.setName("852");
		user.setTeams("");
		userDao.insert(user);
	}

}
