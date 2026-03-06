package com.blueorbit.teamup.controller;


import com.blueorbit.teamup.auth.AuthHelper;
import com.blueorbit.teamup.domain.Application;
import com.blueorbit.teamup.domain.Team;
import com.blueorbit.teamup.service.IApplicationService;
import com.blueorbit.teamup.service.ITeamService;
import com.blueorbit.teamup.service.IWorkflowService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

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
    private final IApplicationService applicationService;
    private final ITeamService teamService;
    private final IWorkflowService workflowService;

    public ApplicationController(IApplicationService applicationService,
                                 ITeamService teamService,
                                 IWorkflowService workflowService) {
        this.applicationService = applicationService;
        this.teamService = teamService;
        this.workflowService = workflowService;
    }


    @PostMapping
    @CrossOrigin
    public Result save(@RequestBody Application application, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        if (application == null || application.getUid() == null || application.getTid() == null) {
            return new Result(Code.PARAM_ERR, null, Msg.PARAM_INVALID);
        }
        if (!Objects.equals(currentUserId, application.getUid())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        Team team = teamService.getById(application.getTid());
        if (team == null) {
            return new Result(Code.SAVE_APPLICATION_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        boolean hasPendingRequest = applicationService.getByUserId(application.getUid()).stream()
                .anyMatch(item -> Objects.equals(item.getTid(), application.getTid())
                        && item.getState() != Application.STATE.DECLINE.ordinal());
        if (hasPendingRequest) {
            return new Result(Code.SAVE_APPLICATION_ERR, null, "Application already exists");
        }
        application.setState(Application.STATE.WAIT.ordinal());
        boolean flag = applicationService.save(application);
        return new Result(flag ? Code.SAVE_APPLICATION_OK : Code.SAVE_APPLICATION_ERR,flag);
    }

    @PutMapping
    @CrossOrigin
    public Result update(@RequestBody Application application, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        if (application == null || application.getId() == null) {
            return new Result(Code.PARAM_ERR, null, Msg.PARAM_INVALID);
        }
        Application dbApplication = applicationService.getById(application.getId());
        if (dbApplication == null) {
            return new Result(Code.UPDATE_APPLICATION_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        Team team = teamService.getById(dbApplication.getTid());
        if (team == null) {
            return new Result(Code.UPDATE_APPLICATION_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        if (!Objects.equals(currentUserId, team.getCreatorId())) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        application.setUid(dbApplication.getUid());
        application.setTid(dbApplication.getTid());
        boolean flag = workflowService.updateApplication(application);
        return new Result(flag ? Code.UPDATE_APPLICATION_OK : Code.UPDATE_APPLICATION_ERR,flag);
    }

    @GetMapping("/{id}")
    @CrossOrigin
    public Result getById(@PathVariable Long id){
        Application application = applicationService.getById(id);
        if (application == null) {
            return new Result(Code.GET_APPLICATION_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        return new Result(Code.GET_APPLICATION_OK,application,"");
    }

    @DeleteMapping("/{id}")
    @CrossOrigin
    public Result deleteById(@PathVariable Long id, HttpServletRequest request){
        Long currentUserId = AuthHelper.currentUserId(request);
        if (currentUserId == null) {
            return new Result(Code.AUTH_ERR, null, Msg.TOKEN_INVALID);
        }
        Application dbApplication = applicationService.getById(id);
        if (dbApplication == null) {
            return new Result(Code.DELETE_APPLICATION_ERR, null, Msg.RESOURCE_NOT_FOUND);
        }
        Team team = teamService.getById(dbApplication.getTid());
        boolean teamCreator = team != null && Objects.equals(currentUserId, team.getCreatorId());
        boolean applicant = Objects.equals(currentUserId, dbApplication.getUid());
        if (!teamCreator && !applicant) {
            return new Result(Code.FORBIDDEN_ERR, null, Msg.NO_PERMISSION);
        }
        boolean flag = applicationService.delete(id);
        return new Result(flag ? Code.DELETE_APPLICATION_OK : Code.DELETE_APPLICATION_ERR,flag);
    }

    @GetMapping
    @CrossOrigin
    public Result getAll(){
        List<Application> applicationList = applicationService.getAll();
        Integer code = null != applicationList ? Code.GET_ALL_APPLICATION_OK : Code.GET_ALL_APPLICATION_ERR;
        String msg = null != applicationList ? "" : "No application list";
        return new Result(code,applicationList,msg);
    }

}

