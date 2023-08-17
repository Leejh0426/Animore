package umc.animore.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umc.animore.model.Town;

import java.util.Optional;

@Repository
public interface TownRepository extends JpaRepository<Town, Long> {

    Town getTownIdByCityAndDistrict(String city, String district); //town_id찾기

    //가게 등록시 town_id
    Optional<Town> findByCityContainingAndDistrict(String city, String district);
}

