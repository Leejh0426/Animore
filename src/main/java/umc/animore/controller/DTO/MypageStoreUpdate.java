package umc.animore.controller.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import umc.animore.model.Town;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MypageStoreUpdate {

    private String storeName;

    private String storeExplain;

    private String storeImageUrl;

    private String open;

    private String close;

    private String dayoff1;
    private String dayoff2;

    private String amount;

    private String storeSignificant;

    private List<String> tags;  //해시태그

    private String storeLocation; //업체 주소
    private String storeNumber; // 업체 번호
    private double latitude; //위도
    private double longitude; //경도
    private Town town;//도시 id


    public MypageStoreUpdate(String storeName, String storeExplain, String storeImageUrl, String open, String close, String dayoff1, String dayoff2, String amount, String storeSignificant, List<String> tags,String storeLocation, String storeNumber, double latitude, double longitude) {
        this.storeName = storeName;
        this.storeExplain = storeExplain;
        this.storeImageUrl = storeImageUrl;
        this.open = open;
        this.close = close;
        this.dayoff1 = dayoff1;
        this.dayoff2 = dayoff2;
        this.amount = amount;
        this.storeSignificant = storeSignificant;
        this.tags = tags;
        this.storeLocation=storeLocation;
        this.storeNumber=storeNumber;
        this.latitude=latitude;
        this.longitude=longitude;
    }

}

