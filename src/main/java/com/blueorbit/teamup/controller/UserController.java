package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.domain.User;
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
@RequestMapping("/users")
public class UserController {
    @Autowired
    private IUserService userService;

    @PostMapping
    public Result save(@RequestBody User user){
        user.setTeams("");
        boolean flag = userService.save(user);
        return new Result(flag ? Code.SAVE_USER_OK : Code.SAVE_USER_ERR,flag);
    }
    @PutMapping
    public Result update(@RequestBody User user){
        boolean flag = userService.update(user);
        return new Result(flag ? Code.UPDATE_USER_OK : Code.UPDATE_USER_ERR,flag);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        User user = userService.getById(id);
        Integer code = null != user ? Code.GET_USER_OK : Code.GET_USER_ERR;
        String msg = null != user ? "" : "No user for this id";
        return new Result(code,user,msg);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        boolean flag = userService.delete(id);
        return new Result(flag ? Code.DELETE_USER_OK : Code.DELETE_USER_ERR,flag);
    }

    @GetMapping
    public Result getAll(){
        List<User> userList = userService.getAll();
        Integer code = null != userList ? Code.GET_ALL_USER_OK : Code.GET_ALL_USER_ERR;
        String msg = null != userList ? "" : "No user list";
        return new Result(code,userList,msg);
    }
}

