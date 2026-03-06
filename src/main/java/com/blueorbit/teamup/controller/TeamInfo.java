package com.blueorbit.teamup.controller;

import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.domain.Comment;
import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.domain.Team;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
@Data
@EqualsAndHashCode(callSuper = false)
public class TeamInfo{
    private Team team;
    private Info info;
    private List<Comment> commentList;
    private List<Application> applicationList;
}
