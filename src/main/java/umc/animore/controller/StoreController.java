package umc.animore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import umc.animore.config.auth.PrincipalDetails;
import umc.animore.config.exception.BaseException;
import umc.animore.config.exception.BaseResponse;
import umc.animore.config.exception.BaseResponseStatus;
import umc.animore.controller.DTO.MypageMemberUpdate;
import umc.animore.controller.DTO.MypageStoreUpdate;
import umc.animore.model.*;
import umc.animore.model.review.StoreDTO;
import umc.animore.repository.ImageRepository;
import umc.animore.service.ImageService;
import umc.animore.service.StoreService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@RestController
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    ImageRepository imageRepository;

    @PostMapping("/manage/store")
    public BaseResponse<MypageStoreUpdate> UpdateStore(@RequestBody MypageStoreUpdate mypageStoreUpdate, @RequestPart(required = false,value = "images") MultipartFile imageFile){
        String imageUrl = null;

        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long storeId = principalDetails.getUser().getStore().getStoreId();

            if(imageFile != null) {
                // 이미지 파일 저장 경로
                String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\templates\\image\\";
                UUID uuid = UUID.randomUUID();
                String originalFileName = uuid + "_" + imageFile.getOriginalFilename();
                File saveFile = new File(projectPath +originalFileName);

                // 이미지 URL 정보를 리스트에 추가
                imageUrl = "http://www.animore.co.kr/reviews/images/" + originalFileName;

                imageFile.transferTo(saveFile);

                // 이미지 메타데이터 DB에 저장
                Image image = new Image();
                image.setImgName(originalFileName);
                image.setImgOriName(imageFile.getOriginalFilename());
                image.setImgPath(saveFile.getAbsolutePath());
                image.setStore(principalDetails.getUser().getStore());
                imageRepository.save(image);
            }

            return new BaseResponse<>(storeService.saveMypageStoreUpdate(mypageStoreUpdate, storeId, imageUrl));

        }catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }catch (IOException e) {
            throw new RuntimeException("이미지 저장에 실패하였습니다.", e);
        }
    }

    @GetMapping("/manage/store")
    public BaseResponse<MypageStoreUpdate> UpdateStore() {
        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long storeId = principalDetails.getUser().getStore().getStoreId();
            Store store = storeService.findStoreId(storeId);

            MypageStoreUpdate MypageStoreUpdate = new MypageStoreUpdate();
            MypageStoreUpdate.setStoreName(store.getStoreName());
            MypageStoreUpdate.setStoreExplain(store.getStoreExplain());
            MypageStoreUpdate.setStoreImageUrl(store.getStoreImageUrl());
            MypageStoreUpdate.setOpen(MinToHour(store.getOpen()));
            MypageStoreUpdate.setClose(MinToHour(store.getClose()));
            MypageStoreUpdate.setDayoff1(EngToDate(store.getDayoff1()));
            MypageStoreUpdate.setDayoff2(EngToDate(store.getDayoff2()));
            MypageStoreUpdate.setAmount("시간당 " + store.getAmount()+"건");
            MypageStoreUpdate.setStoreSignificant(store.getStoreSignificant());
            MypageStoreUpdate.setTags(store.getTags());

            MypageStoreUpdate.setStoreLocation(store.getStoreLocation());
            MypageStoreUpdate.setStoreNumber(store.getStoreNumber());
            MypageStoreUpdate.setLatitude(store.getLatitude());
            MypageStoreUpdate.setLongitude(store.getLongitude());
            MypageStoreUpdate.setTownId(store.getTown().getTownId());

            return new BaseResponse<>(MypageStoreUpdate);

        } catch (Exception exception) {
            return new BaseResponse<>(BaseResponseStatus.SERVER_ERROR);
        }
    }

    private String EngToDate(String day){
        HashMap<String, String> DateMapping = new HashMap<String, String>();
        DateMapping.put("MONDAY", "월요일");
        DateMapping.put("TUESDAY", "화요일");
        DateMapping.put("WEDNESDAY", "수요일");
        DateMapping.put("THURSDAY", "목요일");
        DateMapping.put("FRIDAY", "금요일");
        DateMapping.put("SATURDAY", "토요일");
        DateMapping.put("SUNDAY", "일요일");

        return DateMapping.get(day);
    }

    private String MinToHour(int Time){
        int hour = Time / 60;
        String Min = Integer.toString(Time % 60);
        if (Min.length() == 1){
            Min = "0" + Min;
        }
        String result = hour + ":" + Min;
        return result;
    }
}
