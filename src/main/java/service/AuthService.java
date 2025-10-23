package service;


//! user creation, login validation (+ password hashing?)

import domain.User;
import org.springframework.stereotype.Service;
import repository.AuthRepository;

import java.util.Optional;

@Service
public class AuthService {
    private final AuthRepository userRepository;

    //? we could do smth like this:
    //private final EncryptPassword passwordEncoder = new EncryptPassword(); /

    public AuthService(AuthRepository userRepository) {
        this.userRepository = userRepository;
    }

    //** register, login

    /*
    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

     */
}
