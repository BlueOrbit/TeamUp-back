package com.blueorbit.teamup.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueorbit.teamup.dao.UserDao;
import com.blueorbit.teamup.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private UserDao userDao;

    @GetMapping("/login")
    public String login(@RequestParam("email") String email,
                          @RequestParam("password") String password){
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getEmail,email);
        User user = userDao.selectOne(userLambdaQueryWrapper);
        if (user == null){
            return "No such email!";
        }
        if (user.getPassword().equals(password)){
            return "Success login id "+user.getId();
        } else {
            return "Fail login";
        }
    }

    @GetMapping("/register")
    public String register(@RequestParam("email") String email,
                        @RequestParam("password") String password){
//        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setTeams("");
        user.setName("NEW USER");
        userDao.insert(user);
//        userLambdaQueryWrapper.eq(User::getEmail,email);
//        user = userDao.selectOne(userLambdaQueryWrapper);

        return "Register Success! Your id is " + user.getId();
//        http://localhost:8080/login?email=8&password=9
    }
}
