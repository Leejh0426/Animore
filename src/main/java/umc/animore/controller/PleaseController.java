package umc.animore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import umc.animore.config.auth.PrincipalDetails;
import umc.animore.config.exception.BaseException;
import umc.animore.config.exception.BaseResponse;
import umc.animore.model.Store;
import umc.animore.model.User;
import umc.animore.model.review.StoreDTO;
import umc.animore.repository.ImageRepository;
import umc.animore.repository.StoreRepository;
import umc.animore.repository.UserRepository;
import umc.animore.service.SearchService;

import java.util.*;

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

    //인기순
    @ResponseBody
    @GetMapping("main/search/avg")
    public BaseResponse<List<StoreDTO>> searchByAllAVG(@RequestParam(required = false) String query1,
                                                       @RequestParam(required = false) String query2) {
        try {
            if (isEmpty(query1) && isEmpty(query2)) {
                return new BaseResponse<>(GET_SEARCH_EMPTY_QUERY);
            }

            List<Store> store = new ArrayList<>();

            // query 파라미터 값을 파싱하여 각각의 조건에 따라 처리
            if (query1.startsWith("가게이름:")) {
                String Name = query1.substring("가게이름:".length());
                List<Store> nameMatches = searchService.searchNameReviewsAvgList(Name);
                store.addAll(nameMatches);

            } else if (query1.startsWith("가게주소:")) {
                String storeLocation = query1.substring("가게주소:".length());
                List<Store> locationMatches = searchService.searchLocationReviewsAvgList(storeLocation);
                store.addAll(locationMatches);

            } else if (query1.startsWith("지역:")) {
                String[] parts = query1.substring("지역:".length()).split(",");
                if (parts.length >= 2) {
                    String city = parts[0];
                    String district = parts[1];
                    List<Store> cityMatches = searchService.searchCityListReviewsAvg(city, district);
                    store.addAll(cityMatches);

                } else {
                    return new BaseResponse<>(DATABASE_ERROR);
                }
            } else if (query1.startsWith("해시태그:")) {
                String[] tags = query1.substring("해시태그:".length()).split(",");
                store = searchService.searchTagsReviewsAvg(Arrays.asList(tags));
                // 중복된 가게 제거
                Set<Store> uniqueStores = new HashSet<>(store);
                store = new ArrayList<>(uniqueStores);

                String tagList = String.join(",", tags);

            }else if (query1.startsWith("서비스태그:")) {
                String[] storeSignificant = query1.substring("서비스태그:".length()).split(",");
                store = searchService.searchstoreSignificantReviewsAvg(Arrays.asList(storeSignificant));
                // 중복된 가게 제거
                Set<Store> uniqueStores = new HashSet<>(store);
                store = new ArrayList<>(uniqueStores);

                String tagList = String.join(",", storeSignificant);

            }


            // storeName과 hashtags 값이 있는 경우
            if (!isEmpty(query2)) {
                List<Store> nameMatches = searchService.searchNameReviewsAvgList(query2);
                store.addAll(nameMatches);

            }


            List<StoreDTO> resultStore = convertStoreToDTO(store);
            return new BaseResponse<>(resultStore);

        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isEmptyList(List<?> list) {
        return list == null || list.isEmpty();
    }
}
