package com.blueorbit.teamup.controller;

import com.blueorbit.teamup.domain.User;
import com.blueorbit.teamup.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private IUserService userService;

    @CrossOrigin
    @PostMapping("/login")
    public Result login(@RequestBody User userReceive) {
        System.err.println(userReceive);
        User user = userService.getByEmail(userReceive.getEmail());
        System.err.println(user);
        if (user == null) {
            return new Result(Code.LOGIN_ERR,null,Msg.LOGIN_NO_EMAIL);
        } else if (user.getPassword().equals(userReceive.getPassword())) {
            return new Result(Code.LOGIN_OK,user.getId(),Msg.LOGIN_OK);
        } else {
            return new Result(Code.LOGIN_ERR,null,Msg.LOGIN_WRONG_PASSWORD);
        }
    }
    /* SHOULD NOT USE THIS METHOD */
//    @CrossOrigin
//    @GetMapping("/register")
//    public String register(@RequestParam("email") String email,
//                           @RequestParam("password") String password) {
//        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        User user = new User();
//        user.setEmail(email);
//        user.setPassword(password);
//        user.setTeams("");
//        user.setName("NEW USER");
//        userDao.insert(user);
//        userLambdaQueryWrapper.eq(User::getEmail,email);
//        user = userDao.selectOne(userLambdaQueryWrapper);
//        return "Register Success! Your id is " + user.getId();
//        http://localhost:8080/login?email=8&password=9
//    }
}
