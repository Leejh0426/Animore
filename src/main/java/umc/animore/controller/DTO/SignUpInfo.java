package umc.animore.controller.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

@Getter
@NoArgsConstructor
public class SignUpInfo {
    String address;
    String petname;
    String pettype;
    String petgender;
    String nickname;
    String password;
    String phone;

    String birth = "";
    String specials = "";
    double petWeight = 0.0;
    int age = 0;

}
