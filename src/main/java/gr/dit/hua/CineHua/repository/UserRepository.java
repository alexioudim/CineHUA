package gr.dit.hua.CineHua.repository;

import gr.dit.hua.CineHua.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
    User findById(long id);
    User findByPasscode(int passcode);

}
