package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.domain.Comment;
import com.blueorbit.teamup.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.Date;
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
@CrossOrigin
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private ICommentService commentService;

    @PostMapping
    @CrossOrigin
    public Result save(@RequestBody Comment comment){
        Date date = new Date();
        date.getTime();
        System.out.println(date);
        comment.setDate(date.toString());
        boolean flag = commentService.save(comment);
        return new Result(flag ? Code.SAVE_COMMENT_OK : Code.SAVE_COMMENT_ERR,flag);
    }
    @PutMapping
    @CrossOrigin
    public Result update(@RequestBody Comment comment){
        boolean flag = commentService.update(comment);
        return new Result(flag ? Code.UPDATE_COMMENT_OK : Code.UPDATE_COMMENT_ERR,flag);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public Result getById(@PathVariable Long id){
        Comment comment = commentService.getById(id);
        Integer code = null != comment ? Code.GET_COMMENT_OK : Code.GET_COMMENT_ERR;
        String msg = null != comment ? "" : "No comment for this id";
        return new Result(code,comment,msg);
    }

    @DeleteMapping("/{id}")
    @CrossOrigin
    public Result deleteById(@PathVariable Long id){
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

