package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.dao.InfoDao;
import com.blueorbit.teamup.domain.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/info")
public class InfoController {
    @Autowired
    private InfoDao infoDao;

    @GetMapping("/getinfo/{id}")
    public Info getInfo(@PathVariable int id){
        return infoDao.selectById(id);
    }

}

