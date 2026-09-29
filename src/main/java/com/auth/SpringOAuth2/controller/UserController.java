package com.auth.SpringOAuth2.controller;

import com.auth.SpringOAuth2.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    private static final String URL = "jdbc:sqlite:app.db";

    @PostMapping("/save")
    public String save(@RequestBody User user) throws SQLException {

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS users (
                userId INTEGER PRIMARY KEY AUTOINCREMENT,
                username NVARCHAR(50) UNIQUE NOT NULL,
                email NVARCHAR(50) UNIQUE NOT NULL,
                password NVARCHAR(50) NULL,
                name NVARCHAR(100)
            )
        """);

            String sql = """
            INSERT INTO users(username, email, password, name)
            VALUES(?, ?, ?, ?)
        """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getName());

                ps.executeUpdate();
            }
        }

        return "User saved!";
    }

    @GetMapping("/live")
    public String liveTest() {
        return "Live...";
    }

    @GetMapping("/users")
    public List<User> getAllUsers() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("userId"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));

                users.add(user);
            }
        }

        return users;
    }

    @GetMapping("/users/{username}")
    public User getUser(@PathVariable String username) throws SQLException {

        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("userId"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));

                    return user;
                }
            }
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"
        );
    }

}
