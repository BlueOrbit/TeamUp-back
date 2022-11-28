package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.service.IApplicationService;
import com.blueorbit.teamup.service.ITeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author BlueOrbit
 * @since 2022-11-26
 */
@RestController
@RequestMapping("/applications")
public class ApplicationController {
    @Autowired
    private IApplicationService applicationService;

    @Autowired
    private ITeamService iTeamService;

    @PostMapping
    public Result save(@RequestBody Application application){
        application.setState(Application.STATE.WAIT.ordinal());
        boolean flag = applicationService.save(application);
        return new Result(flag ? Code.SAVE_APPLICATION_OK : Code.SAVE_APPLICATION_ERR,flag);
    }
    @PutMapping
    public Result update(@RequestBody Application application){
        boolean flag = applicationService.update(application);
        if (application.getState() == Application.STATE.ACCEPT.ordinal()){
            flag =flag && iTeamService.addUser(application.getTid(), application.getUid());
        }
        return new Result(flag ? Code.UPDATE_APPLICATION_OK : Code.UPDATE_APPLICATION_ERR,flag);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        Application application = applicationService.getById(id);
        Integer code = null != application ? Code.GET_APPLICATION_OK : Code.GET_APPLICATION_ERR;
        String msg = null != application ? "" : "No application for this id";
        return new Result(code,application,msg);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        boolean flag = applicationService.delete(id);
        return new Result(flag ? Code.DELETE_APPLICATION_OK : Code.DELETE_APPLICATION_ERR,flag);
    }

    @GetMapping
    public Result getAll(){
        List<Application> applicationList = applicationService.getAll();
        Integer code = null != applicationList ? Code.GET_ALL_APPLICATION_OK : Code.GET_ALL_APPLICATION_ERR;
        String msg = null != applicationList ? "" : "No application list";
        return new Result(code,applicationList,msg);
    }

}

