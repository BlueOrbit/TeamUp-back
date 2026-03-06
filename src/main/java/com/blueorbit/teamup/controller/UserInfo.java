package com.blueorbit.teamup.controller;

import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.domain.Comment;
import com.blueorbit.teamup.domain.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserInfo {
    private User user;
    private List<Comment> commentList;
    private List<Application> applicationList;
}
