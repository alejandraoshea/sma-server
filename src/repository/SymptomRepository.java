package repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SymptomRepository {

    private final JdbcTemplate jdbcTemplate;

    public SymptomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //** saveSymptom, ....?
}
