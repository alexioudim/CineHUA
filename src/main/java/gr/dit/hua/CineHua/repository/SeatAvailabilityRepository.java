package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.Screening;
import gr.dit.hua.CineHua.entity.Seat;
import gr.dit.hua.CineHua.entity.SeatAvailability;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {

    @Query("SELECT sa FROM SeatAvailability sa WHERE sa.screening.id = :screeningId")
    List<SeatAvailability> findAllByScreeningId(@Param("screeningId") long screeningId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sa From SeatAvailability sa WHERE sa.id IN :ids")
    List<SeatAvailability> findAllByIdForUpdate(@Param("ids") List<Long> ids);

    @Query("select sa.screening.id from SeatAvailability sa where sa.id = :saId")
    Long findScreeningIdBySeatAvailabilityId(@Param("saId") long saId);


}


