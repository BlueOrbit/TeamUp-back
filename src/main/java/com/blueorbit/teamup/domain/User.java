package com.blueorbit.teamup.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String email;

    private String password;

    private String teams;

    public boolean joinTeam(Long team_id){
        if (teams.length() + team_id.toString().length() < 32){
            teams = teams + team_id.toString();
            return true;//加入成功
        }
        else {
            return false;//teams 长度太长，不能存入数据库
        }

    }

}
