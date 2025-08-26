package gr.dit.hua.CineHua.controller;

import gr.dit.hua.CineHua.config.JwtUtils;
import gr.dit.hua.CineHua.dto.request.LoginRequest;
import gr.dit.hua.CineHua.dto.response.JwtResponse;
import gr.dit.hua.CineHua.entity.Role;
import gr.dit.hua.CineHua.entity.User;
import gr.dit.hua.CineHua.repository.RoleRepository;
import gr.dit.hua.CineHua.repository.UserRepository;
import gr.dit.hua.CineHua.service.UserDetailsImpl;
import gr.dit.hua.CineHua.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {


    private final UserService userService;
    public static boolean changePassword = true;
    AuthenticationManager authenticationManager;
    UserRepository userRepository;
    RoleRepository roleRepository;
    BCryptPasswordEncoder encoder;
    JwtUtils jwtUtils;

    public UserController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder encoder, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @GetMapping("/view")
    public List<User> showUsers() {
        return userService.getUsers();
    }

    @PostMapping("/new")
    public ResponseEntity<String> createUser(@RequestBody User user) {

        changePassword = true;
        String username = userService.saveUser(user);

        return ResponseEntity.ok("User " + username + " created successfully");
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<String> editUser(@PathVariable long id, @RequestBody User user) {
        user.setId(id);
        String username = userService.saveUser(user);

//        User editedUser = userService.editUser(id, user);
//        String username = userService.saveUser(editedUser);
        return ResponseEntity.ok("User " + username + " updated successfully");
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        String username = userService.deleteUser(id);
        return ResponseEntity.ok("User " + username + " deleted successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("authentication");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        System.out.println("authentication: " + authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("post authentication");
        String jwt = jwtUtils.generateJwtToken(authentication);
        System.out.println("jwt: " + jwt);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

}
