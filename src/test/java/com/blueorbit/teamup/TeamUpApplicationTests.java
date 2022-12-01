package com.blueorbit.teamup;

import com.blueorbit.teamup.controller.Code;
import com.blueorbit.teamup.controller.UserController;
import com.blueorbit.teamup.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TeamUpApplicationTests {

	@Test
	public void testCreateUser(){
		UserController userController = new UserController();
		User user = new User();
		user.setEmail("TEST@8888");
		user.setPassword("TEST@PASS");
		assertEquals(Code.SAVE_USER_OK,userController.save(user).getCode());

		user.setEmail("TEST@8888");
		user.setPassword("TEST@PASS");
		assertEquals(Code.SAVE_USER_ERR,userController.save(user).getCode());

		user.setEmail("SHORT");
		user.setPassword("TEST@PASS");
		assertEquals(Code.SAVE_USER_ERR,userController.save(user).getCode());

		user.setEmail("TEST@8888");
		user.setPassword("SHORT");
		assertEquals(Code.SAVE_USER_ERR,userController.save(user).getCode());
	}

	@Test
	public void testGetUserById() {
		UserController userController = new UserController();
		User user = (User) userController.getById(1L).getData();
		assertEquals(1L, user.getId().longValue());
	}
}
