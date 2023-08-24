package umc.animore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import umc.animore.config.exception.BaseException;
import umc.animore.config.exception.BaseResponse;
import umc.animore.model.Store;
import umc.animore.model.review.StoreDTO;
import umc.animore.repository.ImageRepository;
import umc.animore.repository.StoreRepository;
import umc.animore.repository.UserRepository;
import umc.animore.service.SearchService;

import java.util.ArrayList;
import java.util.List;

import static umc.animore.config.exception.BaseResponseStatus.*;

@RestController
public class PleaseController {

    @Autowired
    SearchService searchService;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StoreRepository storeRepository;

    private List<StoreDTO> convertStoreToDTO(List<Store> storeList) {
        List<StoreDTO> storeDTOList = new ArrayList<>();

        for (Store store : storeList) {
            StoreDTO storeDTO = new StoreDTO();
            storeDTO.setStoreId(store.getStoreId());
            storeDTO.setStoreName(store.getStoreName());
            storeDTO.setStoreExplain(store.getStoreExplain());
            storeDTO.setStoreLocation(store.getStoreLocation());
            storeDTO.setStoreImageUrl(store.getStoreImageUrl());
            storeDTO.setStoreNumber(store.getStoreNumber());
            storeDTO.setStoreRecent(store.getStoreRecent());
            storeDTO.setStoreLike(store.getStoreLike());
            storeDTO.setCreateAt(store.getCreateAt());
            storeDTO.setModifyAt(store.getModifyAt());
            storeDTO.setLatitude(store.getLatitude());
            storeDTO.setLongitude(store.getLongitude());
            storeDTO.setDiscounted(store.isDiscounted());
            storeDTO.setOpen(store.getOpen());
            storeDTO.setClose(store.getClose());
            storeDTO.setAmount(store.getAmount());
            storeDTO.setDayoff1(store.getDayoff1());
            storeDTO.setDayoff2(store.getDayoff2());
            storeDTO.setTags(store.getTags());
            storeDTO.setStoreSignificant(store.getStoreSignificant());

            storeDTOList.add(storeDTO);
        }

        return storeDTOList;
    }

    @ResponseBody
    @GetMapping("/main/search")
    public BaseResponse<List<StoreDTO>> searchMainByTown(@RequestParam("city") String city, @RequestParam("district") String district) {
        try {
            if (district == null || district.equals("") || city == null || city.equals("")) {
                return new BaseResponse<>(GET_SEARCH_EMPTY_QUERY);
            }
            if (district.length() > 50 || city.length() > 50) {
                return new BaseResponse<>(GET_SEARCH_INVALID_QUERY1);
            }

            List<Store> store = searchService.searchCityList(city,district);

            System.out.println("query: " + city + " " + district);
            System.out.println("가게정보: " + store);

            if (store.isEmpty()) {
                return new BaseResponse<>(DATABASE_ERROR);
            }

            List<StoreDTO> resultStore = convertStoreToDTO(store);

            return new BaseResponse<>(resultStore);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
