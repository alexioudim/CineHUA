package gr.dit.hua.CineHua.service;

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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            throw new UsernameNotFoundException(username);
        } else {
            User user = opt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.getRoles()
                            .stream()
                            .map(role -> new SimpleGrantedAuthority(role.toString()))
                            .collect(Collectors.toSet())
            );
        }

    }

    @Transactional
    public long saveUser(User user) {
        String password = user.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        Optional<Role> optionalRole = roleRepository.findByName("ROLE_EMPLOYEE");
        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
            user = userRepository.save(user);
            return user.getUser_id();
        } else {
            throw new EntityNotFoundException("Role not found");
        }

    }
}
