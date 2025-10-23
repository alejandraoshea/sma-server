package repository;

//! database access for users

import domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public class UserRepository {
    private final Map<String, User> fakeDb = new HashMap<>();

    public void save(User user) {
        if (fakeDb.containsKey(user.getEmail())) {
            throw new RuntimeException("User already exists");
        }
        fakeDb.put(user.getEmail(), user);
        System.out.println("User saved (in-memory): " + user.getEmail());
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(fakeDb.get(email));
    }

    public List<User> findAll() {
        return new ArrayList<>(fakeDb.values());
    }
}
