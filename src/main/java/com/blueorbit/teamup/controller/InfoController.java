package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.domain.Info;
import com.blueorbit.teamup.service.IInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/info")
public class InfoController {
    @Autowired
    private IInfoService iInfoService;

    @GetMapping("/getinfo/{id}")
    public Info getInfo(@PathVariable Long id){
        return iInfoService.getById(id);
    }

    @PostMapping("/search")
    public List<Info> searchInfoContent(@RequestBody Info info){
        return iInfoService.getByContent(info.getContent());
    }

}

