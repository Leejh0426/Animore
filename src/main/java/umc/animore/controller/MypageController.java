package umc.animore.controller;

import com.fasterxml.jackson.databind.JsonNode;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import umc.animore.config.auth.PrincipalDetails;
import umc.animore.config.exception.BaseException;
import umc.animore.config.exception.BaseResponse;
import umc.animore.controller.DTO.*;
import umc.animore.model.Image;
import umc.animore.model.Pet;
import umc.animore.model.Reservation;
import umc.animore.model.User;
import umc.animore.repository.DTO.ReservationInfoMapping;
import umc.animore.service.ImageService;
import umc.animore.service.PetService;
import umc.animore.service.ReservationService;
import umc.animore.service.UserService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.sql.Array;
import java.util.*;

import static umc.animore.config.exception.BaseResponseStatus.*;

@RestController
@RequestMapping("/api")
public class MypageController {

    @Autowired
    private PetService petService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;




    /**
     * 마이페이지 첫화면 API
     */
    @GetMapping("/mypage")
    public BaseResponse<MypageHome> mypagehome(){
        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principalDetails.getUser();
            Long userId = principalDetails.getUser().getId();
            Pet pet = petService.findTop1ByUser_idOrderByPetId(userId);

            if(pet == null){
                pet = new Pet();
            }


            
            List<ReservationInfoMapping> reservationInfoMappings = reservationService.findByUserIdOrderByStartTimeDesc(userId);
            List<Map<Long, Object>> storeId_ImageUrl = imageService.findImageByReservationId(reservationInfoMappings);


            Image img = imageService.findImageByUserId(userId);

            if(img ==null){
                img = new Image();
            }

            MypageHome mypageHome = MypageHome.builder()
                    .nickname(user.getNickname())
                    .petName(pet.getPetName())
                    .petAge(pet.getPetAge())
                    .petType(pet.getPetType())
                    .storeId_ImageUrl(storeId_ImageUrl)
                    .proFileImgUrl("http://www.animore.co.kr/reviews/images/"+img.getImgName())
                    .build();


            return new BaseResponse<>(mypageHome);

        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());

        }
    }







    /**
     * 마이페이지 - 프로필수정 click -> 와이어프레임.프로필수정 API
     */
    // 이미지는 어떻게해야하나?? 일단은 주소를 반환
    @GetMapping("/mypage/profile")
    public BaseResponse<MypageProfile> mypageProfile() {

            PrincipalDetails principalDetails = (PrincipalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principalDetails.getUser();

            MypageProfile mypageProfile = MypageProfile.builder()
                    .imageUrls("http://www.animore.co.kr/reviews/images/"+user.getImage().getImgName())
                    .nickname(user.getNickname())
                    .aboutMe(user.getAboutMe())
                    .build();

            System.out.println(System.getProperty("user.dir"));
            return new BaseResponse<>(mypageProfile);


    }

    /**
     *  와이어프레임.프로필수정 - 수정하기 API
     */
    @PutMapping("/mypage/profile")
    public BaseResponse<MypageProfile> profileupdate(@RequestPart(required = false) MultipartFile multipartFile,@RequestPart(required = false) String nickname, @RequestPart(required = false) String aboutMe, @Value("${upload.path}") String url) throws IOException {

        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = principalDetails.getUser();
            Long userId = user.getId();

            if(multipartFile != null) {
                imageService.saveImage(multipartFile, userId, url);
            }

            if(nickname != null || aboutMe != null) {
                user = userService.saveNicknameAboutMe(userId, nickname, aboutMe);
            }

            MypageProfile mypageProfile = MypageProfile.builder()
                    .nickname(user.getNickname())
                    .aboutMe(user.getAboutMe())
                    .imageUrls("https://www.animore.co.kr/reviews/images/"+user.getImage().getImgName())
                    .build();



            return new BaseResponse<>(mypageProfile);

        }catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }




    /**
     * 와이어프레임.회원정보수정 - 비밀번호 확인 API
     */
    @GetMapping("/mypage/member/user/password")
    public BaseResponse<String> userUpdatePasswordCheck(@RequestParam("password") String password){
        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userPassword = principalDetails.getUser().getPassword();

            if (bCryptPasswordEncoder.matches(password,userPassword)) {

                return new BaseResponse<>(SUCCESS);
            }else{

                throw new BaseException(GET_USER_PASSWORD_ERROR);
            }
        }
        catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }



    /**
     * 와이어프레임.회원정보수정 API
     */
    @GetMapping("/mypage/member/user")
    public BaseResponse<MypageMember> userupdate(){

        PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principalDetails.getUser();

        MypageMember mypageMember = MypageMember.builder()
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .email(user.getEmail())
                .gender(user.getGender())
                .build();

        return new BaseResponse<>(mypageMember);



    }



    /**
     * 와이어프레임.회원정보수정 - 수정하기 API
     */
    @PutMapping("/mypage/member/user")
    public BaseResponse<MypageMember> userupdate(@RequestBody(required = false) MypageMemberUpdate mypageMemberUpdate){

        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = principalDetails.getUser().getId();



            return new BaseResponse<>(userService.saveMypageMemberUpdate(mypageMemberUpdate, userId));

        }catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }


    /**
     * 와이어프레임.반려동물수정 API
     */
    @GetMapping("/mypage/member/pet")
    public BaseResponse<List<MypagePetUpdate>> petupdate(){
        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = principalDetails.getUser().getId();


            return new BaseResponse<>(petService.findMypageMPetUpdateByUserId(userId));
        }catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 와이어프레임.반려동물수정 - 반려동물수정하기 API
     */
    @PutMapping("/mypage/member/pet")
    public BaseResponse<MypagePetUpdate> petupdate(@RequestBody(required = false) MypagePetUpdate mypagePetUpdate){
        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = principalDetails.getUser().getId();



            return new BaseResponse<>(petService.saveMypagePetUpdate(mypagePetUpdate, userId));

        }catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @DeleteMapping("/mypage/member/remove")
    public BaseResponse<String> memberCancel(){
        try{
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = principalDetails.getUser().getId();

            userService.memberCancel(userId);

            return new BaseResponse<>(SUCCESS);

        }catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }

    }



/* 프로필 이미지조회 방법 임시세이브

// 이미지는 어떻게해야하나?? 일단은 주소를 반환
@GetMapping("/mypage/profile")
public HttpEntity<LinkedMultiValueMap<String, Object>> mypageProfile(@Value("${upload.path}") String url) {
    try {
        PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = principalDetails.getUser();
        Long userId = user.getId();
        Image img = imageService.findImageByUserId(userId);


        MypageProfile mypageProfile = MypageProfile.builder()
                .nickname(user.getNickname())
                .aboutMe(user.getAboutMe())
                .build();

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        HttpStatus httpStatus = HttpStatus.CREATED;

        map.add("user관련 값", new BaseResponse<>(mypageProfile));

        map.add("사진 바이너리 값",
                new FileSystemResource(url + img.getImgName()));


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);


        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(map, headers);


        return requestEntity;

    }catch(BaseException e){
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        HttpStatus httpStatus = HttpStatus.CREATED;

        map.add("에러관련 값", new BaseResponse<>(e.getStatus()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(map, headers);
        return requestEntity;
    }

}


 */



}
