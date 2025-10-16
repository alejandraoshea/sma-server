package controller;

import domain.User;
import org.springframework.http.ResponseEntity;
import service.AuthService;
import org.springframework.web.bind.annotation.*;

//will be handling authentication: login, register and logout + interacts with AuthService and AuthRepo
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*@PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String token = authService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public User getUserProfile(@RequestParam String username) {
        return authService.getUserByUsername(username);
    }

     */

}
