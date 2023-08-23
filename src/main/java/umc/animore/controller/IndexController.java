package umc.animore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import umc.animore.config.auth.PrincipalDetails;
import umc.animore.config.exception.BaseException;
import umc.animore.config.exception.BaseResponse;
import umc.animore.controller.DTO.SignUpInfo;
import umc.animore.model.Pet;
import umc.animore.model.User;
import umc.animore.repository.UserRepository;
import umc.animore.service.PetService;
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

    @Autowired
    private PetService petService;


    @GetMapping("/user/loginoauth")
    public String Oauth(@RequestParam("firm") String firm) {

        if (firm == "google") {
            return "redirect:https://animore.co.kr/oauth2/authorization/google";
        }
        else if (firm == "naver") {
            return "redirect:https://animore.co.kr/oauth2/authorization/naver";
        }
        else if (firm == "kakao") {
            return "redirect:https://animore.co.kr/oauth2/authorization/kakao";
        }
        else if (firm == "facebook") {
            return "redirect:https://animore.co.kr/oauth2/authorization/facebook";
        }

        return null;
    }


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

    // 추가회원가입
    @PostMapping("/signup")
    @ResponseBody
    public BaseResponse<?> signupForm(@RequestBody SignUpInfo signUpInfo) {

        PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principalDetails.getUser();

        if (user.getPassword() != null){
            return new BaseResponse<>(ALREADY_REGISTERED_USER);
        }

        Pet pet = new Pet();
        pet.setUser(user);

        if (signUpInfo.getPassword() == null) {
            return new BaseResponse<>(PASSWORD_EMPTY_ERROR);
        }


        try {

            if (4 >  signUpInfo.getPassword().length() || signUpInfo.getPassword().length() > 16) {
                return new BaseResponse<>(PASSWORD_INPUT_ERROR);
            }

            if (signUpInfo.getAddress() == null) {
                return new BaseResponse<>(ADDRESS_INPUT_ERROR);
            }
            if(signUpInfo.getPetname() == null) {
                return new BaseResponse<>(PETNAME_INPUT_ERROR);
            }
            if (signUpInfo.getPettype() == null) {
                return new BaseResponse<>(PETTYPE_INPUT_ERROR);
            }
            if (signUpInfo.getNickname() == null) {
                return new BaseResponse<>(NICKNAME_INPUT_ERROR);
            }
            if (signUpInfo.getPhone() == null) {
                return new BaseResponse<>(PHONE_INPUT_ERROR);
            }


            if (!signUpInfo.getBirth().isEmpty()) {user.setBirthday(signUpInfo.getBirth());}
            if (!signUpInfo.getSpecials().isEmpty()) {pet.setPetSpecials(signUpInfo.getSpecials());}
            if (signUpInfo.getPetWeight() != 0.0) {pet.setPetWeight(pet.getPetWeight());}
            if (signUpInfo.getAge() != 0) {pet.setPetAge(signUpInfo.getAge());}
            petService.save(pet);

            user.setRole("ROLE_USER");

            String rawPassword = signUpInfo.getPassword();
            String encPassword = bCryptPasswordEncoder.encode(rawPassword);
            user.setPassword(encPassword);
            userService.save(user);
            userService.singupForm(user.getId(), signUpInfo.getAddress(), signUpInfo.getPetname(), signUpInfo.getPettype(), signUpInfo.getPetgender(), signUpInfo.getNickname(), signUpInfo.getPhone());
            return new BaseResponse<>(SUCCESS);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
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
