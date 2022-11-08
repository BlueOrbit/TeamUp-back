package com.blueorbit.teamup.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("info")
public class Info implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long team_id;

    private String course;

    private Integer number_limit;

    private String content;


}
