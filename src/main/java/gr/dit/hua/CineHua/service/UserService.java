package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.controller.UserController;
import gr.dit.hua.CineHua.entity.Role;
import gr.dit.hua.CineHua.entity.User;
import gr.dit.hua.CineHua.repository.RoleRepository;
import gr.dit.hua.CineHua.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(BCryptPasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> opt = userRepository.findByUsername(username);

        if (opt.isEmpty())
            throw new UsernameNotFoundException("User with username: " + username + " not found !");
        else {
            User user = opt.get();

            return UserDetailsImpl.build(user);
        }
    }

    @Transactional
    public List<User> getUsers(){
        return userRepository.findAll();
    }

    @Transactional
    public String saveUser(User user) {
        // Αν το password είναι μη κενό και όχι null, τότε κάνε encode
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
        } else {
            // Διατήρησε τον παλιό κωδικό από τη βάση
            User existing = userRepository.findById(user.getId()).orElseThrow();
            user.setPassword(existing.getPassword());
        }

        // Resolve roles by name
        Set<Role> resolvedRoles = new HashSet<>();
        for (Role r : user.getRoles()) {
            Optional<Role> found = roleRepository.findByName(r.getName());
            if (found.isEmpty()) {
                throw new EntityNotFoundException("Role not found: " + r.getName());
            }
            resolvedRoles.add(found.get());
        }
        user.setRoles(resolvedRoles);

        userRepository.save(user);
        return user.getUsername();
    }

    @Transactional
    public String deleteUser(long id) {
        User user = userRepository.findById(id).get();
        userRepository.delete(user);
        return user.getUsername();
    }

}
