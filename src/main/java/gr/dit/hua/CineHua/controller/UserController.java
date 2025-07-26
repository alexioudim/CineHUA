package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.entity.Role;
import gr.dit.hua.CineHua.entity.User;
import gr.dit.hua.CineHua.repository.RoleRepository;
import gr.dit.hua.CineHua.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void setup() {
        Role role_employee = new Role("ROLE_EMPLOYEE");
        Role role_manager = new Role("ROLE_MANAGER");
        Role role_admin  = new Role("ROLE_ADMIN");

        roleRepository.updateOrInsert(role_employee);
        roleRepository.updateOrInsert(role_manager);
        roleRepository.updateOrInsert(role_admin);
    }

    @PostMapping("/new")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            userService.saveUser(user);
            return ResponseEntity.ok("User " + user.getFirstName() + " " + user.getSurName() + " created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("JsonProcessingException" + e.getMessage());
        }
    }


}
