
package com.college.config;

import java.util.Map;

import com.college.dao.UserDAO;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserDAO userDAO;

    public AppUserDetailsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Map<String, Object> user =
                userDAO.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found: " + username);
        }

        String storedUsername =
                user.get("username").toString();

        String storedPassword =
                user.get("password").toString();

        String role =
                user.get("role").toString().toUpperCase();

        return User.builder()
                .username(storedUsername)
                .password(storedPassword)
                .roles(role)
                .build();
    }
}
