package gr.dit.hua.CineHua.service;

import gr.dit.hua.CineHua.entity.Role;
import gr.dit.hua.CineHua.entity.User;
import gr.dit.hua.CineHua.repository.RoleRepository;
import gr.dit.hua.CineHua.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class InitialDataService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void setup() {
        // Initialize roles
        Role roleEmployee = new Role("ROLE_EMPLOYEE");
        Role roleManager  = new Role("ROLE_MANAGER");
        Role roleAdmin    = new Role("ROLE_ADMIN");

        roleRepository.updateOrInsert(roleEmployee);
        roleRepository.updateOrInsert(roleManager);
        roleRepository.updateOrInsert(roleAdmin);

        // Initialize admin
        Optional<User> existing = userRepository.findByUsername("admin");
        if (existing.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode(adminPassword));
            admin.setFirstName("CineHUA");
            admin.setSurName("Admin");
            admin.setEmail("admin@cinehua.gr");
            admin.setPhoneNumber("1234567890");

            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByName("ROLE_ADMIN").get());
            roles.add(roleRepository.findByName("ROLE_EMPLOYEE").get());
            roles.add(roleRepository.findByName("ROLE_MANAGER").get());
            admin.setRoles(roles);

            userRepository.save(admin);
        }
    }
}
