package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.auth.AuthHelper;
import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.domain.Comment;
import com.blueorbit.teamup.domain.User;
import com.blueorbit.teamup.service.IApplicationService;
import com.blueorbit.teamup.service.ICommentService;
import com.blueorbit.teamup.service.IUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final IUserService userService;
    private final ICommentService commentService;
    private final IApplicationService applicationService;
    private final PasswordEncoder passwordEncoder;

    public UserController(IUserService userService,
                          ICommentService commentService,
                          IApplicationService applicationService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.commentService = commentService;
        this.applicationService = applicationService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @CrossOrigin
    public Result save(@RequestBody User user) {
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            return new Result(Code.PARAM_ERR, null, Msg.PARAM_INVALID);
        }
        if (null != userService.getByEmail(user.getEmail())) {
            return new Result(Code.SAVE_USER_ERR, null, Msg.USER_ALREADY_EXIST);
        } else if (user.getEmail().length() < 8) {
            return new Result(Code.SAVE_USER_ERR,null,Msg.EMAIL_TOO_SHORT);
        } else if (!user.getEmail().contains("@")) {
            return new Result(Code.SAVE_USER_ERR, null, Msg.EMAIL_TOO_SHORT);
        } else if (user.getPassword().length() < 6) {
            return new Result(Code.SAVE_USER_ERR,null,Msg.PASSWORD_TOO_SHORT);
        }else {
            user.setTeams("");
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName("NEW USER");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            boolean flag = userService.save(user);
            return new Result(flag ? Code.SAVE_USER_OK : Code.SAVE_USER_ERR, flag);
        }
    }

    @PutMapping
    @CrossOrigin
    public Result update(@RequestBody User user, HttpServletRequest request) {
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null || user == null || user.getId() == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        if (!Objects.equals(currentUserId, user.getId())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        User dbUser = userService.getById(user.getId());
        if (dbUser == null) {
            return new Result(Code.GET_USER_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        if (user.getEmail() != null && !user.getEmail().equals(dbUser.getEmail())) {
            User sameEmailUser = userService.getByEmail(user.getEmail());
            if (sameEmailUser != null && !Objects.equals(sameEmailUser.getId(), user.getId())) {
                return new Result(Code.UPDATE_USER_ERR, null, Msg.USER_ALREADY_EXIST);
            }
        }
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        user.setTeams(dbUser.getTeams());
        boolean flag = userService.update(user);
        return new Result(flag ? Code.UPDATE_USER_OK : Code.UPDATE_USER_ERR, flag);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public Result getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return new Result(Code.GET_USER_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        userInfo.setCommentList(commentService.getByUserId(id));
        userInfo.setApplicationList(applicationService.getByUserId(id));
        return new Result(Code.GET_USER_OK, userInfo, "");
    }

    @DeleteMapping("/{id}")
    @CrossOrigin
    public Result deleteById(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        if (!Objects.equals(currentUserId, id)) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        boolean flag = userService.delete(id);
        return new Result(flag ? Code.DELETE_USER_OK : Code.DELETE_USER_ERR, flag);
    }

    @GetMapping
    @CrossOrigin
    public Result getAll() {
        List<User> userList = userService.getAll();
        Integer code = null != userList ? Code.GET_ALL_USER_OK : Code.GET_ALL_USER_ERR;
        String msg = null != userList ? "" : "No user list";
        List<Comment> allComments = commentService.getAll();
        Map<Long, List<Comment>> commentsByUid = allComments.stream()
                .collect(Collectors.groupingBy(Comment::getSenderId));
        List<Application> allApplications = applicationService.getAll();
        Map<Long, List<Application>> applicationsByUid = allApplications.stream()
                .collect(Collectors.groupingBy(Application::getUid));
        List<UserInfo> userInfoList = new ArrayList<>();
        for (User user:userList) {
            UserInfo tmp = new UserInfo();
            tmp.setUser(user);
            tmp.setCommentList(commentsByUid.getOrDefault(user.getId(), Collections.emptyList()));
            tmp.setApplicationList(applicationsByUid.getOrDefault(user.getId(), Collections.emptyList()));
            userInfoList.add(tmp);
        }
        return new Result(code, userInfoList, msg);
    }
}

