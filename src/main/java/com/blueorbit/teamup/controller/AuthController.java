package com.blueorbit.teamup.controller;

import com.blueorbit.teamup.auth.AuthConstants;
import com.blueorbit.teamup.auth.SessionTokenService;
import com.blueorbit.teamup.domain.User;
import com.blueorbit.teamup.service.IUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService sessionTokenService;

    public AuthController(IUserService userService,
                          PasswordEncoder passwordEncoder,
                          SessionTokenService sessionTokenService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenService = sessionTokenService;
    }

    @CrossOrigin
    @PostMapping("/login")
    public Result login(@RequestBody User userReceive) {
        if (userReceive == null || userReceive.getEmail() == null || userReceive.getPassword() == null) {
            return new Result(Code.PARAM_ERR, null, Msg.PARAM_INVALID);
        }
        User user = userService.getByEmail(userReceive.getEmail());
        if (user == null) {
            return new Result(Code.LOGIN_ERR,null,Msg.LOGIN_NO_EMAIL);
        }
        boolean passwordMatched;
        String savedPassword = user.getPassword();
        if (savedPassword != null && savedPassword.startsWith("$2")) {
            passwordMatched = passwordEncoder.matches(userReceive.getPassword(), savedPassword);
        } else {
            passwordMatched = savedPassword != null && savedPassword.equals(userReceive.getPassword());
            if (passwordMatched) {
                user.setPassword(passwordEncoder.encode(userReceive.getPassword()));
                userService.update(user);
            }
        }
        if (passwordMatched) {
            String token = sessionTokenService.issueToken(user.getId());
            Map<String, Object> payload = new HashMap<>();
            payload.put("uid", user.getId());
            payload.put("token", token);
            return new Result(Code.LOGIN_OK, payload, Msg.LOGIN_OK);
        }
        return new Result(Code.LOGIN_ERR,null,Msg.LOGIN_WRONG_PASSWORD);
    }

    @CrossOrigin
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String auth = request.getHeader(AuthConstants.AUTH_HEADER);
        if (auth != null && auth.startsWith(AuthConstants.BEARER_PREFIX)) {
            sessionTokenService.revoke(auth.substring(AuthConstants.BEARER_PREFIX.length()).trim());
        }
        return new Result(Code.AUTH_OK, true, "Logout success");
    }
}
