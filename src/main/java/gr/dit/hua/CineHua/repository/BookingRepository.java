package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Boolean existsByBookingCode(String bookingCode);

    Booking findByBookingCode(String bookingCode);
}
