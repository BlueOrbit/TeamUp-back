package com.blueorbit.teamup.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blueorbit.teamup.domain.Application;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-02
 */
@Mapper
public interface ApplicationDao extends BaseMapper<Application> {

}