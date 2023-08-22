package umc.animore.model.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import umc.animore.model.Reservation;
import umc.animore.repository.ReservationRepository;
import umc.animore.service.EmailService;
import umc.animore.service.ReservationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@EnableScheduling
public class ReservationScheduling {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ReservationService reservationService;

    @Scheduled(fixedDelay = 60 * 30 * 1000)
    public void checkReservations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMinutes(120);
        List<Reservation> reservations = reservationRepository.findByStartTimeBetweenAndEmailSent(now, end, false);

        for (Reservation r: reservations) {

            String dogsize = r.getDogSize().equals(Reservation.DogSize.MEDIUM) ? "중소형견" : "대형견";
            String cutstyle = "";
            String bathstyle = "";
            switch (r.getCutStyle()) {
                case SCISSORS_CUT:
                    cutstyle = "가위컷";
                    break;
                case MACHINE_CUT:
                    cutstyle = "기계컷";
                    break;
                case SPOTTING_CUT:
                    cutstyle = "스포팅";
                    break;
                case CLIPPING_CUT:
                    cutstyle = "클리핑";
                    break;
                case PARTICAL_CUT:
                    cutstyle = "부분미용";
                    break;
                default:
                    cutstyle = "미정";
            }
            switch (r.getBathStyle()) {
                case BATH:
                    bathstyle = "목욕";
                    break;
                case HEALING:
                    bathstyle = "힐링스파";
                    break;
                case CARBONATED:
                    bathstyle = "탄산스파";
                    break;
                default:
                    bathstyle = "미정";
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 HH시");
            String startTimeFormatted = r.getStartTime().format(formatter);
            String emailContent = String.format(
                    "안녕하세요 회원님! 오늘은 %s 예약당일입니다.\n\n 🔻예약시간 : %s\n 🔻 주소 : %s\n 🔻 옵션 : %s, %s, %s\n늦지않게 방문 부탁드립니다 :)",
                    r.getStore().getStoreName(),
                    startTimeFormatted,
                    r.getAddress(),
                    dogsize,
                    cutstyle,
                    bathstyle
            );

            emailService.sendEmail(r.getUser().getEmail(), "[Animore] 방문 당일 안내", emailContent);
            reservationService.setEmailSent(r.getReservationId());
        }
    }
}
