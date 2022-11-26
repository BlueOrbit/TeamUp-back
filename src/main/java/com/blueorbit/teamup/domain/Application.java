package com.blueorbit.teamup.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("application")
public class Application implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long uid;

    private Long tid;

    private String msg;

    public enum STATE
    {
        WAIT, ACCEPT, DECLINE;
    }
    private int state;
}
