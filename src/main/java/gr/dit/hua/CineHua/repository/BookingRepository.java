package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Boolean existsByBookingCode(String bookingCode);

    Booking findByBookingCode(String bookingCode);

    Booking findByPaymentIntentId(String paymentIntentId);

    @Query("select b.id from Booking b")
    Page<Long> findPageIds(Pageable pageable);

    // 2) Φέρε πλήρη γράφημα για συγκεκριμένα ids
    @Query("""
        select distinct b
        from Booking b
        left join fetch b.tickets t
        left join fetch t.seatAvailability sa
        left join fetch sa.seat s
        left join fetch sa.screening sc
        left join fetch sc.movie m
        left join fetch sc.auditorium a
        where b.id in :ids
        """)
    List<Booking> findAllWithDetailsByIdIn(Collection<Long> ids);

    // 3) Αναζήτηση ενός booking με όλα τα details
    @Query("""
        select b
        from Booking b
        left join fetch b.tickets t
        left join fetch t.seatAvailability sa
        left join fetch sa.seat s
        left join fetch sa.screening sc
        left join fetch sc.movie m
        left join fetch sc.auditorium a
        where b.bookingCode = :code
        """)
    Booking findOneWithDetailsByBookingCode(String code);
}
