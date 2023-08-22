package umc.animore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import java.util.List;
import java.util.UUID;

@RestController
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    ImageRepository imageRepository;

    @PostMapping("/manage/store")
    public BaseResponse<MypageStoreUpdate> UpdateStore(  @RequestParam String storeName,
                                                         @RequestParam String storeExplain,
                                                         @RequestParam (required = false) String storeImageUrl,
                                                         @RequestParam String open,
                                                         @RequestParam String close,
                                                         @RequestParam(required = false, value = "dayoff1") String dayoff1,
                                                         @RequestParam(required = false, value = "dayoff2") String dayoff2,
                                                         @RequestParam(required = false, value = "amount") String amount,
                                                         @RequestParam(required = false, value = "storeSignificant") List<String> storeSignificant,
                                                         @RequestParam(required = false, value = "tags") List<String> tags,
                                                         @RequestParam String storeLocation,
                                                         @RequestParam(required = false, value = "storeNumber") String storeNumber,
                                                         @RequestParam(required = false, value = "latitude") double latitude,
                                                         @RequestParam(required = false, value = "longitude") double longitude,
                                                         @RequestPart(required = false,value = "images") MultipartFile imageFile){
        String imageUrl = storeImageUrl;

        try {
            // 현재 사용자의 정보를 가져옴
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long storeId = principalDetails.getUser().getStore().getStoreId();

            // 이미지가 이미 존재하는지 확인
            Image existingImage = imageRepository.findByStoreId(storeId);

            if(imageFile != null) {
                // 이미지 파일 저장 경로
                String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\templates\\image\\";
                UUID uuid = UUID.randomUUID();
                String originalFileName = uuid + "_" + imageFile.getOriginalFilename();
                File saveFile = new File(projectPath +originalFileName);

                // 이미지 URL 정보를 리스트에 추가
                imageUrl = "http://www.animore.co.kr/reviews/images/" + originalFileName;
                imageFile.transferTo(saveFile);

                if (existingImage != null) {
                    // 기존 이미지 업데이트
                    existingImage.setImgName(originalFileName);
                    existingImage.setImgOriName(imageFile.getOriginalFilename());
                    existingImage.setImgPath(saveFile.getAbsolutePath());
                    imageRepository.save(existingImage);
                    imageUrl="http://www.animore.co.kr/reviews/images/" + existingImage.getImgName();
                } else {
                    // 새로운 이미지 저장
                    Image image = new Image();
                    image.setImgName(originalFileName);
                    image.setImgOriName(imageFile.getOriginalFilename());
                    image.setImgPath(saveFile.getAbsolutePath());
                    image.setStore(principalDetails.getUser().getStore());
                    imageRepository.save(image);
                }
            }

            MypageStoreUpdate mypageStoreUpdate = new MypageStoreUpdate(
                    storeName, storeExplain, imageUrl, open, close, dayoff1, dayoff2, amount,
                    storeSignificant, tags, storeLocation, storeNumber, latitude, longitude,0L
            );

            return new BaseResponse<>(storeService.saveMypageStoreUpdate(mypageStoreUpdate, storeId));

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
