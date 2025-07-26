package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
//    User findByPasscode(int passcode);
    User findById(long id);

}
