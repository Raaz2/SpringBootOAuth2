package com.auth.SpringOAuth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Controller {

    @GetMapping("/new")
    public Map<String, Object> getUserDetails(@AuthenticationPrincipal OAuth2User principal) {
        return principal != null ? principal.getAttributes() : Map.of("error", "No user logged in");
    }

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @GetMapping("/check-id")
    public String checkGoogleClientId() {
        return "Resolved Client ID: " + googleClientId;
    }
    @GetMapping(value = "/", produces = "text/html")
    public String renderProfile(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "<h2>No active session found. <a href='/login'>Please Login</a></h2>";
        }

        // Resolves differences between Google and GitHub attributes
        String name = principal.getAttribute("name");
        if (name == null) {
            name = principal.getAttribute("login"); // GitHub fallback
        }

        String email = principal.getAttribute("email");
        if (email == null) {
            email = "Not available / Private";
        }

        String pictureUrl = principal.getAttribute("picture"); // Google
        if (pictureUrl == null) {
            pictureUrl = principal.getAttribute("avatar_url"); // GitHub
        }

        // Replaced '50%' with '9999px' to eliminate formatting clashes
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>User Profile</title>
                <style>
                    body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f4f6f8; }
                    .card { background: white; padding: 24px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); text-align: center; width: 280px; }
                    img { border-radius: 9999px; width: 96px; height: 96px; margin-bottom: 12px; border: 2px solid #ddd; object-fit: cover; }
                    h2 { margin: 8px 0; font-size: 20px; }
                    p { color: #666; margin: 4px 0; font-size: 14px; word-break: break-all; }
                </style>
            </head>
            <body>
                <div class="card">
                    <img src="%s" alt="Profile Picture" referrerpolicy="no-referrer" />
                    <h2>%s</h2>
                    <p>%s</p>
                </div>
            </body>
            </html>
            """.formatted(pictureUrl, name, email);
    }
}