package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.CreditNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {

    CreditNote findByCreditNoteId(Long creditNoteId);
}
