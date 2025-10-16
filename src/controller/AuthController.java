package controller;

import domain.User;
import org.springframework.http.ResponseEntity;
import service.AuthService;
import org.springframework.web.bind.annotation.*;

//will be handling authentication: login, register and logout + interacts with AuthService and AuthRepo
@RestController
@RequestMapping("/api/authentication")
@CrossOrigin(origins = "*") //for frontend
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*@PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        authService.register(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        Optional<User> user = authService.login(email, password);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }
     */

}
