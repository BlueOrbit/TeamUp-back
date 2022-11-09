package com.blueorbit.teamup.controller;

import com.blueorbit.teamup.domain.Comment;
import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.domain.Team;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
@Data
@EqualsAndHashCode(callSuper = false)
public class TeamInfo{
    public Team team;
    public Info info;
    public List<Comment> commentList;
}
