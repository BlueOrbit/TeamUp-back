package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.domain.User;
import com.blueorbit.teamup.service.IApplicationService;
import com.blueorbit.teamup.service.ICommentService;
import com.blueorbit.teamup.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-02
 */
@RestController
@CrossOrigin
@RequestMapping("/users")
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private ICommentService commentService;
    @Autowired
    private IApplicationService applicationService;

    @PostMapping
    @CrossOrigin
    public Result save(@RequestBody User user) {
        if (null != userService.getByEmail(user.getEmail())) {
            return new Result(Code.SAVE_USER_ERR, null, Msg.USER_ALREADY_EXIST);
        } else if (user.getEmail().length() < 8) {
            return new Result(Code.SAVE_USER_ERR,null,Msg.EMAIL_TOO_SHORT);
        } else if (user.getPassword().length() < 6) {
            return new Result(Code.SAVE_USER_ERR,null,Msg.PASSWORD_TOO_SHORT);
        }else {
            user.setTeams("");
            boolean flag = userService.save(user);
            return new Result(flag ? Code.SAVE_USER_OK : Code.SAVE_USER_ERR, flag);
        }
    }

    @PutMapping
    @CrossOrigin
    public Result update(@RequestBody User user) {
        boolean flag = userService.update(user);
        return new Result(flag ? Code.UPDATE_USER_OK : Code.UPDATE_USER_ERR, flag);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public Result getById(@PathVariable Long id) {
        User user = userService.getById(id);
        Integer code = null != user ? Code.GET_USER_OK : Code.GET_USER_ERR;
        String msg = null != user ? "" : "No user for this id";
        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        userInfo.setCommentList(commentService.getByUserId(user.getId()));
        userInfo.setApplicationList(applicationService.getByUserId(user.getId()));
        return new Result(code, userInfo, msg);
    }

    @DeleteMapping("/{id}")
    @CrossOrigin
    public Result deleteById(@PathVariable Long id) {
        boolean flag = userService.delete(id);
        return new Result(flag ? Code.DELETE_USER_OK : Code.DELETE_USER_ERR, flag);
    }

    @GetMapping
    @CrossOrigin
    public Result getAll() {
        List<User> userList = userService.getAll();
        Integer code = null != userList ? Code.GET_ALL_USER_OK : Code.GET_ALL_USER_ERR;
        String msg = null != userList ? "" : "No user list";
        List<UserInfo> userInfoList = new ArrayList<>();
        for (User user:userList
             ) {
            UserInfo tmp = new UserInfo();
            tmp.setUser(user);
            tmp.setCommentList(commentService.getByUserId(user.getId()));
            tmp.setApplicationList(applicationService.getByUserId(user.getId()));
            userInfoList.add(tmp);
        }
        return new Result(code, userInfoList, msg);
    }
}

