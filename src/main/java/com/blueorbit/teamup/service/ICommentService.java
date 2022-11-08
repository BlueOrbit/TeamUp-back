package com.blueorbit.teamup.service;

import com.blueorbit.teamup.domain.Comment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-08
 */
@Transactional
public interface ICommentService {
    public boolean save(Comment comment);

    public boolean update(Comment comment);

    public boolean delete(Long id);

    public Comment getById(Long id);

    public List<Comment> getAll();
}
