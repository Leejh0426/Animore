package umc.animore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import umc.animore.config.exception.BaseException;
import umc.animore.config.exception.BaseResponse;
import umc.animore.model.User;
import umc.animore.repository.UserRepository;
import umc.animore.service.UserService;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static umc.animore.config.exception.BaseResponseStatus.*;


@Controller
public class IndexController {


    @Autowired
    private UserService userService;


    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/")
    @ResponseBody
    public BaseResponse<String> Oauthreturn(@RequestParam String token){
        return new BaseResponse<>(token);
    }


    @PostMapping("/join")
    @ResponseBody
    public BaseResponse<String> join(@RequestBody  User user, HttpServletResponse response){
        try {
            System.out.println(user);

            if(user.getUsername() == null){
                throw new BaseException(GET_USER_EMPTY_USERNAME);
            }

            if(user.getPassword()== null){
                throw new BaseException(GET_USER_PASSWORD_ERROR);
            }
            user.setRole("ROLE_MANAGER");
            String rawPassword = user.getPassword();
            String encPassword = bCryptPasswordEncoder.encode(rawPassword);
            user.setPassword(encPassword);
            userService.save(user);

            String redirect_uri="https://animore.co.kr/loginForm";
            response.sendRedirect(redirect_uri);

            throw new BaseException(REDIRECT_ERROR);
        }
        catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }catch(IOException e){
            return new BaseResponse<>("IO에러");
        }

    }



    @GetMapping("/jenkins")
    @ResponseBody
    public String jenkinstest() {
        return "jenkins test";
    }

    @GetMapping("/loginForm")
    public String loginForm(){
        return "loginForm";
    }

    @GetMapping("/loginFormManager")
    public String manageLoginForm(){
        return "loginFormManager";
    }

    @GetMapping("/user")
    @ResponseBody
    public String user(){
        return "user==========================";
    }

    @GetMapping("/admin")
    @ResponseBody
    public String admin(){
        return "admin";
    }

    @GetMapping("/manager")
    @ResponseBody
    public String manager(){
        return "manager";
    }



    @GetMapping("/joinForm")
    public String joinForm(){
        return "joinForm";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/info")
    @ResponseBody
    public String info(){
        return "개인정보";
    }

    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/data")
    @ResponseBody
    public String data(){
        return "데이터";
    }


}
