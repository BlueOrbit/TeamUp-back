package com.blueorbit.teamup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueorbit.teamup.dao.CommentDao;
import com.blueorbit.teamup.domain.Comment;
import com.blueorbit.teamup.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-08
 */
@Service
public class CommentServiceImpl implements ICommentService {
    @Autowired
    private CommentDao commentDao;

    @Override
    public boolean save(Comment comment) {
        commentDao.insert(comment);
        return true;
    }

    @Override
    public boolean update(Comment comment) {
        commentDao.updateById(comment);
        return true;
    }

    @Override
    public boolean delete(Long id) {
        commentDao.deleteById(id);
        return true;
    }

    @Override
    public Comment getById(Long id) {
        return commentDao.selectById(id);
    }

    @Override
    public List<Comment> getByTeamId(Long id) {
        LambdaQueryWrapper<Comment> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Comment::getTeamId,id);
        return commentDao.selectList(lqw);
    }

    @Override
    public List<Comment> getAll() {
        return commentDao.selectList(null);
    }
}
