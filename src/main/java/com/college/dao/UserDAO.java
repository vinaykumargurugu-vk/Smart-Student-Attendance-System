
package com.college.dao;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> findByUsername(String username) {

        String sql = """
            SELECT user_id, username, password, role,
                   student_id, faculty_id
            FROM users
            WHERE username = ?
            """;

        List<Map<String, Object>> users =
                jdbcTemplate.queryForList(sql, username);

        if (users.isEmpty()) {
            return null;
        }

        return users.get(0);
    }
}
