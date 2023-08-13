package umc.animore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import umc.animore.config.auth.PrincipalDetails;
import umc.animore.config.exception.BaseException;
import umc.animore.config.exception.BaseResponse;
import umc.animore.config.exception.BaseResponseStatus;
import umc.animore.controller.DTO.MypageMemberUpdate;
import umc.animore.controller.DTO.MypageStoreUpdate;
import umc.animore.model.Store;
import umc.animore.model.User;
import umc.animore.model.review.StoreDTO;
import umc.animore.service.StoreService;

import java.util.HashMap;

@RestController
public class StoreController {

    @Autowired
    private StoreService storeService;

    @PostMapping("/manage/store")
    public BaseResponse<MypageStoreUpdate> UpdateStore(@RequestBody MypageStoreUpdate mypageStoreUpdate){

        try {
            PrincipalDetails principalDetails = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long storeId = principalDetails.getUser().getStore().getStoreId();

            return new BaseResponse<>(storeService.saveMypageStoreUpdate(mypageStoreUpdate, storeId));

        }catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
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
