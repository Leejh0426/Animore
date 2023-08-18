package umc.animore.model.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import umc.animore.model.Reservation;
import umc.animore.repository.ReservationRepository;
import umc.animore.service.EmailService;

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

    @Scheduled(fixedDelay = 60 * 30 * 1000)
    public void checkReservations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMinutes(120);
        List<Reservation> reservations = reservationRepository.findByStartTimeBetween(now, end);

        for (Reservation r: reservations) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 HH시");
            String startTimeFormatted = r.getStartTime().format(formatter);
            String emailContent = String.format(
                    "안녕하세요 회원님! 오늘은 %s 예약당일입니다.\n\n 🔻예약시간 : %s\n 🔻 주소 : %s\n 🔻 옵션 : %s, %s, %s\n늦지않게 방문 부탁드립니다 :)",
                    r.getStore().getStoreName(),
                    startTimeFormatted,
                    r.getAddress(),
                    r.getPet_type(),
                    r.getCutStyle(),
                    r.getBathStyle()
            );

            emailService.sendEmail(r.getUser().getEmail(), "[Animore] 방문 당일 안내", emailContent);
        }
    }
}
