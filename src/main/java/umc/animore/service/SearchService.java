package umc.animore.service;


import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import umc.animore.config.auth.PrincipalDetails;
import umc.animore.config.exception.BaseException;
import umc.animore.model.*;
import umc.animore.repository.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static umc.animore.config.exception.BaseResponseStatus.*;


@Service
public class SearchService {
    @Autowired
    private SearchRespository searchRespository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private TownRepository townRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SearchStoreRepository searchStoreRepository;

    @Autowired
    StoreRepository storeRepository;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SearchService(SearchRespository searchRespository, SearchHistoryRepository searchHistoryRepository, TownRepository townRepository, ReviewRepository reviewRepository, LocationRepository locationRepository){
        this.searchRespository = searchRespository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.townRepository =townRepository;
        this.reviewRepository = reviewRepository;
        this.locationRepository = locationRepository;
    }

    //가게이름
    public List<Store> searchNameList(String storeName) throws BaseException {
        try {
            List<Store> store = searchRespository.findByStoreNameContaining(storeName);
            return store;
        }catch (Exception exception) {
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //가게이름 인기순
    public List<Store> searchNameBestList(String storeName) throws BaseException {
        try {
            List<Store> store = searchRespository.findByStoreNameContainingOrderByStoreLikeDesc(storeName);
            return store;
        }catch (Exception exception) {
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //가게이름 후기 많은 순
    public List<Store> searchNameMostReviewsList(String storeName) throws BaseException {
        try {
            List<Store> store = searchRespository.findStoresWithMostReviewsByStoreNameContaining(storeName);
            return store;
        }catch (Exception exception) {
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //가게이름 후기 평점 높은 순
    public List<Store> searchNameReviewsAvgList(String storeName) throws BaseException {
        try {
            List<Store> store = searchRespository.findStoresWithHighestAverageScoreByStoreNameContaining(storeName);
            return store;
        }catch (Exception exception) {
            throw new BaseException(RESPONSE_ERROR);
        }
    }



    //주소
    public List<Store> searchLocationList(String storeLocation) throws BaseException {
        try{
            List<Store> store = searchRespository.findByStoreLocationContaining(storeLocation);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //주소 인기순
    public List<Store> searchLocationBestList(String storeLocation) throws BaseException {
        try{
            List<Store> store = searchRespository.findByStoreLocationContainingOrderByStoreLikeDesc(storeLocation);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //주소 후기 많은 순
    public List<Store> searchLocationMostReviewsList(String storeLocation) throws BaseException {
        try{
            List<Store> store = searchRespository.findStoresWithMostReviewsByStoreLocationContaining(storeLocation);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //주소 후기 평점 평균 높은 순
    public List<Store> searchLocationReviewsAvgList(String storeLocation) throws BaseException {
        try{
            List<Store> store = searchRespository.findStoresWithHighestAverageScoreByStoreLocationContaining(storeLocation);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }


    //시도
    public List<Store> searchCityList(String city, String district) throws BaseException {

        try {
            Town town= townRepository.getTownIdByCityAndDistrict(city, district);
            System.out.println("가게정보: " + town);

            List<Store> store = searchRespository.findByTown(town);
            return store;
        } catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //시도 인기순
    public List<Store> searchCityListBest(String city, String district) throws BaseException {
        try {
            Town town = townRepository.getTownIdByCityAndDistrict(city, district);
            System.out.println("가게정보: "+town);

            List<Store> store = searchRespository.findByTownOrderByStoreLikeDesc(town);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //시도 후기 많은 순
    public List<Store> searchCityListMostReviews(String city, String district) throws BaseException {
        try {
            Town town = townRepository.getTownIdByCityAndDistrict(city, district);
            System.out.println("가게정보: "+town);

            List<Store> store = searchRespository.findStoresWithMostReviewsByTown(town);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //시도 후기 평점 평균 높은 순
    public List<Store> searchCityListReviewsAvg(String city, String district) throws BaseException {
        try {
            Town town = townRepository.getTownIdByCityAndDistrict(city, district);
            System.out.println("가게정보: "+town);

            List<Store> store = searchRespository.findStoresWithHighestAverageScoreByTown(town);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //리뷰
    public List<Store> getStoresWithMostReviews() throws BaseException {

        try {
            List<Store> stores = searchRespository.findStoresWithMostReviews();
            stores.forEach(store -> Hibernate.initialize(store.getTown()));
            return stores;
        } catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }

    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구의 반지름 (단위: km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c;

        return distance;
    }

    //거리순 - 가게이름
        //* 가게이름을 완전히 똑같게 입력하지않으면 equals 작동안되지않나?
        //** 목록 갱신할때 CLEAR()해버리면 가장 가까운 가게만 List에 남을텐데 의도한건가?

    public List<Store> recommendNearestStore(String storeName) throws BaseException {
        try {
            Optional<Location> optionalLocation = Optional.ofNullable(locationRepository.findByLocationId(1L));
            if (optionalLocation.isPresent()) {
                Location currentLocation = optionalLocation.get();

                List<Store> allStores = searchRespository.findAll();

                // 이름을 포함하는 가게 목록을 찾습니다.
                List<Store> storesWithName = new ArrayList<>();

                for (Store store : allStores) {
                    if (store.getStoreLocation().equals(storeName)) {
                        // 가게와 현재 위치 간의 거리 계산
                        storesWithName.add(store);
                    }
                }

                if (!storesWithName.isEmpty()) {
                    storesWithName.sort((store1, store2) -> {
                        double distance1 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store1.getLatitude(), store1.getLongitude());
                        double distance2 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store2.getLatitude(), store2.getLongitude());
                        double threshold = 0.001; // 거리가 0.001 이하인 경우 동일한 거리로 간주

                        if (Math.abs(distance1 - distance2) < threshold) {
                            return 0;
                        } else {
                            return Double.compare(distance1, distance2);
                        }
                    });
                }
                else {
                    // 가게 이름을 포함하는 가게 목록이 없는 경우에도 해당 가게를 리스트에 추가합니다.
                    Store selfStore = allStores.stream()
                            .filter(store -> store.getStoreName().equals(storeName))
                            .findFirst()
                            .orElse(null);

                    if (selfStore != null) {
                        storesWithName.add(selfStore);
                    }
                }

                // 가게 이름을 포함하는 가게 목록을 반환합니다.
                return storesWithName;

            }
            return null;
        } catch (Exception exception) {
            throw new BaseException(NO_MATCHING_STORE);
        }
    }


    //거리순 - 가게주소
    public List<Store> recommendNearestStoreLocation(String storeLocation) throws BaseException {
        try {
            Optional<Location> optionalLocation = Optional.ofNullable(locationRepository.findByLocationId(1L));
            if (optionalLocation.isPresent()) {
                Location currentLocation = optionalLocation.get();

                List<Store> allStores = searchRespository.findAll();

                // 지역를 포함하는 가게 목록을 찾습니다.
                List<Store> storesWithLocation = new ArrayList<>();

                for (Store store : allStores) {
                    if (store.getStoreLocation().equals(storeLocation)) {
                        // 가게와 현재 위치 간의 거리 계산
                        storesWithLocation.add(store);
                    }
                }
                // 주소를 포함하는 가게 목록을 거리 순으로 정렬합니다.
                if (!storesWithLocation.isEmpty()) {
                    Collections.sort(storesWithLocation, (store1, store2) -> {
                        double distance1 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store1.getLatitude(), store1.getLongitude());
                        double distance2 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store2.getLatitude(), store2.getLongitude());
                        double threshold = 0.001; // 거리가 0.001 이하인 경우 동일한 거리로 간주

                        if (Math.abs(distance1 - distance2) < threshold) {
                            return 0;
                        } else {
                            return Double.compare(distance1, distance2);
                        }
                    });
                }
                else {
                    // 가게 주소을 포함하는 가게 목록이 없는 경우에도 해당 가게를 리스트에 추가합니다.
                    Store selfStore = allStores.stream()
                            .filter(store -> store.getStoreLocation().equals(storeLocation))
                            .findFirst()
                            .orElse(null);

                    if (selfStore != null) {
                        storesWithLocation.add(selfStore);
                    }
                }

                return storesWithLocation;
            }
            return null;
        } catch (Exception exception) {
            throw new BaseException(NO_MATCHING_STORE);
        }
    }


    //거리순 - 가게시도
    public List<Store> recommendNearestStoreTown(String city, String district) throws BaseException {
        try {
            Town town = townRepository.getTownIdByCityAndDistrict(city, district);
            Optional<Location> optionalLocation = Optional.ofNullable(locationRepository.findByLocationId(1L));
            if (optionalLocation.isPresent()) {
                Location currentLocation = optionalLocation.get();

                List<Store> allStores = searchRespository.findAll();

                // 지역를 포함하는 가게 목록을 찾습니다.
                List<Store> storesWithTown = new ArrayList<>();

                for (Store store : allStores) {
                    if (store.getTown().equals(town)) {
                        storesWithTown.add(store);
                    }
                }

                if (!storesWithTown.isEmpty()) {
                    // 시도를 포함하는 가게 목록을 거리 순으로 정렬합니다.
                    Collections.sort(storesWithTown, (store1, store2) -> {
                        double distance1 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store1.getLatitude(), store1.getLongitude());
                        double distance2 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store2.getLatitude(), store2.getLongitude());
                        double threshold = 0.001; // 거리가 0.001 이하인 경우 동일한 거리로 간주

                        if (Math.abs(distance1 - distance2) < threshold) {
                            return 0;
                        } else {
                            return Double.compare(distance1, distance2);
                        }
                    });
                }
                else{
                    // 가게 지역을 포함하는 가게 목록이 없는 경우에도 해당 가게를 리스트에 추가합니다.
                    Store selfStore = allStores.stream()
                            .filter(store -> store.getTown().equals(town))
                            .findFirst()
                            .orElse(null);

                    if (selfStore != null) {
                        storesWithTown.add(selfStore);
                    }

                }

                return storesWithTown;
            }
            return null;
        } catch (Exception exception) {
            throw new BaseException(NO_MATCHING_STORE);
        }
    }



    //최근 검색기록 (3개씩)
    public List<SearchHistory> searchHistory(User user) throws BaseException {
        try {
            List<SearchHistory> searchHistoryRes = searchHistoryRepository.findByUserOrderBySearchCreateAtDesc(user);
            return searchHistoryRes;
        }catch (Exception exception){
            throw  new BaseException(RESPONSE_ERROR);
        }
    }


    public void postSearchHistory(User user, String searchQuery) {
        List<SearchHistory> searchHistoryList = searchHistoryRepository.findByUserOrderBySearchCreateAtDesc(user);

        if (searchHistoryList.size() < 3) {
            saveQuery(user, searchQuery);
        } else {
            SearchHistory oldestSearchHistory = searchHistoryList.get(searchHistoryList.size() - 1);
            searchHistoryRepository.delete(oldestSearchHistory);
            saveQuery(user, searchQuery);
        }
    }



    private void saveQuery(User user, String searchQuery) {
        SearchHistory searchHistory = new SearchHistory();

        // 현재 시간을 기준으로 Timestamp 객체 생성
        Timestamp timestamp = Timestamp.from(Instant.now());

        searchHistory.setUser(user);
        searchHistory.setSearchQuery(searchQuery);

        //SearchHistory 객체에 Timestamp 할당
        searchHistory.setSearchCreateAt(timestamp);
        searchHistoryRepository.save(searchHistory);
    }


    //예약 많은 순
    public List<Store> searchReservationMost() throws BaseException {
        try{
            List<Store> store = searchRespository.findStoresWithMostReservations();
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //가게 검색 기록(3개씩)
    public List<SearchStore> searchStore(User user) throws BaseException{
        try{
            List<SearchStore> searchStoreRes = searchStoreRepository.findByUserOrderBySearchCreateAtDesc(user);
            return searchStoreRes;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }


    public void postSearchStoreHistory(User user, Store store){
        List<SearchStore> searchStores = searchStoreRepository.findByUserOrderBySearchCreateAtDesc(user);

        if(searchStores.size()<3){
            saveStoreRecord(user,store);
        }else{
            SearchStore oldestSearchStore = searchStores.get(searchStores.size()-1);
            searchStoreRepository.delete(oldestSearchStore);
            saveStoreRecord(user,store);
        }
    }


    private void saveStoreRecord(User user, Store store){
        SearchStore searchStore = new SearchStore();

        // 현재 시간을 기준으로 Timestamp 객체 생성
        Timestamp timestamp = Timestamp.from(Instant.now());

        searchStore.setSearchCreateAt(timestamp);
        searchStore.setUser(user);
        searchStore.setStore(store);
        searchStoreRepository.save(searchStore);

    }

    //해시태그로 가게찾음
    public List<Store> searchStoresBytags(List<String> tags) throws BaseException{
        try{
            List<Store> store = storeRepository.findByTagsIn(tags);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }

    }

    //해시태그 인기순
    public List<Store> searchTagsBest(List<String> tags) throws BaseException {
        try {

            List<Store> store = searchRespository.findByTagsInOrderByStoreLikeDesc(tags);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //해시태그 후기 많은 순
    public List<Store> searchTagsMostReviews(List<String> tags) throws BaseException {
        try {
            List<Store> store = searchRespository.findStoresWithMostReviewsByTagsIn(tags);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //해시태그 후기별점 평균 순
    public List<Store> searchTagsReviewsAvg(List<String> tags) throws BaseException {
        try {
            List<Store> store = searchRespository.findStoresWithHighestAverageScoreByTagsIn(tags);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }


    //해시태그 거리순
    public List<Store> recommendNearestHashTags(List<String> tags) throws BaseException {
        try {
            Optional<Location> optionalLocation = Optional.ofNullable(locationRepository.findByLocationId(1L));
            if (optionalLocation.isPresent()) {
                Location currentLocation = optionalLocation.get();

                List<Store> allStores = searchRespository.findAll();

                // 해시태그를 포함하는 가게 목록을 찾습니다.
                List<Store> storesWithTags = new ArrayList<>();

                for (Store store : allStores) {
                    if (store.getTags().containsAll(tags)) {
                        storesWithTags.add(store);
                    }
                }

                if (!storesWithTags.isEmpty()) {
                    // 태그를 포함하는 가게 목록을 거리 순으로 정렬합니다.
                    Collections.sort(storesWithTags, (store1, store2) -> {
                        double distance1 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store1.getLatitude(), store1.getLongitude());
                        double distance2 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store2.getLatitude(), store2.getLongitude());
                        double threshold = 0.001; // 거리가 0.001 이하인 경우 동일한 거리로 간주

                        if (Math.abs(distance1 - distance2) < threshold) {
                            return 0;
                        } else {
                            return Double.compare(distance1, distance2);
                        }
                    });
                }
                else {
                    // 가게 해시태그를 포함하는 가게 목록이 없는 경우에도 해당 가게를 리스트에 추가합니다.
                    Store selfStore = allStores.stream()
                            .filter(store -> store.getTags().equals(tags))
                            .findFirst()
                            .orElse(null);

                    if (selfStore != null) {
                        storesWithTags.add(selfStore);
                    }

                }

                return storesWithTags;
            }
            return null;
        } catch (Exception exception) {
            throw new BaseException(NO_MATCHING_STORE);
        }
    }



    //태그편집으로 가게찾음
    public List<Store> searchStoresBySignificantIn(List<String> storeSignificant) throws BaseException{
        try{
            List<Store> store = storeRepository.findByStoreSignificantIn(storeSignificant);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }

    }

    //태그 인기순
    public List<Store> searchstoreSignificantBest(List<String> storeSignificant) throws BaseException {
        try {

            List<Store> store = searchRespository.findByStoreSignificantInOrderByStoreLikeDesc(storeSignificant);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //태그 후기 많은 순
    public List<Store> searchstoreSignificantMostReviews(List<String> storeSignificant) throws BaseException {
        try {
            List<Store> store = searchRespository.findStoresWithMostReviewsByStoreSignificantIn(storeSignificant);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    //태그 후기별점 평균 순
    public List<Store> searchstoreSignificantReviewsAvg(List<String> storeSignificant) throws BaseException {
        try {
            List<Store> store = searchRespository.findStoresWithHighestAverageScoreByStoreSignificantIn(storeSignificant);
            return store;
        }catch (Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }


    // 태그 거리순
    public List<Store> recommendNeareststoreSignificant(List<String> storeSignificant) throws BaseException {
        try {
            Optional<Location> optionalLocation = Optional.ofNullable(locationRepository.findByLocationId(1L));
            if (optionalLocation.isPresent()) {
                Location currentLocation = optionalLocation.get();

                List<Store> allStores = searchRespository.findAll();

                // 태그를 포함하는 가게 목록을 찾습니다.
                List<Store> storesWithTags = new ArrayList<>();

                for (Store store : allStores) {
                    if (store.getStoreSignificant().containsAll(storeSignificant)) {
                        storesWithTags.add(store);
                    }
                }

                if (!storesWithTags.isEmpty()) {
                    // 태그를 포함하는 가게 목록을 거리 순으로 정렬합니다.
                    Collections.sort(storesWithTags, (store1, store2) -> {
                        double distance1 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store1.getLatitude(), store1.getLongitude());
                        double distance2 = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                store2.getLatitude(), store2.getLongitude());
                        double threshold = 0.001; // 거리가 0.001 이하인 경우 동일한 거리로 간주

                        if (Math.abs(distance1 - distance2) < threshold) {
                            return 0;
                        } else {
                            return Double.compare(distance1, distance2);
                        }
                    });
                }
                else{
                    // 가게 해시태그를 포함하는 가게 목록이 없는 경우에도 해당 가게를 리스트에 추가합니다.
                    Store selfStore = allStores.stream()
                            .filter(store -> store.getTags().equals(storeSignificant))
                            .findFirst()
                            .orElse(null);

                    if (selfStore != null) {
                        storesWithTags.add(selfStore);
                    }
                }

                return storesWithTags;
            }
            return null;
        } catch (Exception exception) {
            throw new BaseException(NO_MATCHING_STORE);
        }
    }
}
