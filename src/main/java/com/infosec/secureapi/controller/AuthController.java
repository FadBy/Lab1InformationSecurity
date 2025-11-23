package com.infosec.secureapi.controller;

import com.infosec.secureapi.dto.LoginRequest;
import com.infosec.secureapi.dto.LoginResponse;
import com.infosec.secureapi.dto.RegisterRequest;
import com.infosec.secureapi.entity.User;
import com.infosec.secureapi.service.DataService;
import com.infosec.secureapi.service.JwtService;
import com.infosec.secureapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                         UserService userService,
                         JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User newUser = userService.createUser(
                    registerRequest.getUsername(),
                    registerRequest.getPassword()
            );

            UserDetails userDetails = userService.loadUserByUsername(newUser.getUsername());
            String token = jwtService.generateToken(userDetails);

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(DataService.escapeHtml(newUser.getUsername()));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = userService.findByUsername(loginRequest.getUsername());
            UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
            String token = jwtService.generateToken(userDetails);

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(DataService.escapeHtml(user.getUsername()));

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body("Invalid username or password");
        }
    }
}

