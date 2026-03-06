package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.auth.AuthHelper;
import com.blueorbit.teamup.domain.Comment;
import com.blueorbit.teamup.service.ICommentService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-02
 */
@RestController
@CrossOrigin
@RequestMapping("/comments")
public class CommentController {
    private final ICommentService commentService;

    public CommentController(ICommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @CrossOrigin
    public Result save(@RequestBody Comment comment, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        if (comment == null || comment.getSenderId() == null || !Objects.equals(currentUserId, comment.getSenderId())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        comment.setDate(Instant.now().toString());
        boolean flag = commentService.save(comment);
        return new Result(flag ? Code.SAVE_COMMENT_OK : Code.SAVE_COMMENT_ERR,flag);
    }
    @PutMapping
    @CrossOrigin
    public Result update(@RequestBody Comment comment, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        if (comment == null || comment.getId() == null) {
            return new Result(Code.PARAM_ERR, null, Msg.PARAM_INVALID);
        }
        Comment dbComment = commentService.getById(comment.getId());
        if (dbComment == null) {
            return new Result(Code.GET_COMMENT_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        if (!Objects.equals(currentUserId, dbComment.getSenderId())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        comment.setSenderId(dbComment.getSenderId());
        comment.setTeamId(dbComment.getTeamId());
        boolean flag = commentService.update(comment);
        return new Result(flag ? Code.UPDATE_COMMENT_OK : Code.UPDATE_COMMENT_ERR,flag);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public Result getById(@PathVariable Long id){
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return new Result(Code.GET_COMMENT_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        return new Result(Code.GET_COMMENT_OK,comment,"");
    }

    @DeleteMapping("/{id}")
    @CrossOrigin
    public Result deleteById(@PathVariable Long id, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        Comment dbComment = commentService.getById(id);
        if (dbComment == null) {
            return new Result(Code.DELETE_COMMENT_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        if (!Objects.equals(currentUserId, dbComment.getSenderId())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        boolean flag = commentService.delete(id);
        return new Result(flag ? Code.DELETE_COMMENT_OK : Code.DELETE_COMMENT_ERR,flag);
    }

    @GetMapping
    @CrossOrigin
    public Result getAll(){
        List<Comment> commentList = commentService.getAll();
        Integer code = null != commentList ? Code.GET_ALL_COMMENT_OK : Code.GET_ALL_COMMENT_ERR;
        String msg = null != commentList ? "" : "No comment list";
        return new Result(code,commentList,msg);
    }

}

