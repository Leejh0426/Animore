package umc.animore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import umc.animore.config.exception.BaseException;
import umc.animore.controller.DTO.MypagePetUpdate;
import umc.animore.model.Pet;
import umc.animore.model.User;
import umc.animore.repository.PetRepository;
import umc.animore.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static umc.animore.config.exception.BaseResponseStatus.GET_PET_EMPTY_ERROR;
import static umc.animore.config.exception.BaseResponseStatus.RESPONSE_ERROR;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;


    public Pet findByUserId(Long userId){
        return petRepository.findByUser_id(userId);
    }

    public Pet getPetInfo(User user) {
        return petRepository.findByUser(user);
    }




    @Transactional
    public Pet findTop1ByUser_idOrderByPetId(Long userId){
        return petRepository.findTop1ByUser_idOrderByPetId(userId);
    }




    /**
     * userId로 n개의 pet 조회 후 MypagePetUpdate로 반환
     */
    @Transactional
    public List<MypagePetUpdate> findMypageMPetUpdateByUserId(Long userId) throws BaseException {

        try {

            User user = userRepository.findById(userId);

            List<Pet> pets = user.getPets();
            System.out.println(pets);

            List<MypagePetUpdate> mypagePetUpdates = new ArrayList<MypagePetUpdate>();

            for (Pet pet : pets) {
                MypagePetUpdate mypagePetUpdate = MypagePetUpdate.builder()
                        .petId(pet.getPetId())
                        .petWeight(pet.getPetWeight())
                        .petAge(pet.getPetAge())
                        .petGender(pet.getPetGender())
                        .petName(pet.getPetName())
                        .petSpecials(pet.getPetSpecials())
                        .petType(pet.getPetType())
                        .build();

                mypagePetUpdates.add(mypagePetUpdate);
            }

            if (mypagePetUpdates.size()<1) {
                throw new BaseException(GET_PET_EMPTY_ERROR);
            }

            return mypagePetUpdates;

        }catch(BaseException e){
            throw new BaseException(GET_PET_EMPTY_ERROR);
        }

        catch(Exception e){
            throw new BaseException(RESPONSE_ERROR);
        }

    }


    /**
     * mypagePetUpdate를 이용하여 Pet 업데이트
     */

    @Transactional
    public MypagePetUpdate saveMypagePetUpdate(MypagePetUpdate mypagePetUpdate,Long userId) throws BaseException{

        try {

            User user = userRepository.findById(userId);
            List<Pet> pets = user.getPets();
            Pet pet = null;

            for (Pet tmp : pets) {
                if (tmp.getPetId() == mypagePetUpdate.getPetId()) {
                    pet = tmp;
                }
            }

            if(pet == null){
                throw new BaseException(GET_PET_EMPTY_ERROR);
            }


            pet.setPetAge(mypagePetUpdate.getPetAge());
            pet.setPetName(mypagePetUpdate.getPetName());
            pet.setPetGender(mypagePetUpdate.getPetGender());
            pet.setPetType(mypagePetUpdate.getPetType());
            pet.setPetSpecials(mypagePetUpdate.getPetSpecials());
            pet.setPetWeight(mypagePetUpdate.getPetWeight());

            return mypagePetUpdate;

        }catch(BaseException e){
            throw new BaseException(GET_PET_EMPTY_ERROR);
        }

        catch(Exception e){
            throw new BaseException(RESPONSE_ERROR);
        }

    }
}
