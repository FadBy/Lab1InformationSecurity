package com.infosec.secureapi.controller;

import com.infosec.secureapi.dto.LoginRequest;
import com.infosec.secureapi.dto.LoginResponse;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        boolean isNewUser = false;
        User user;
        try {
            user = userService.findByUsername(loginRequest.getUsername());
        } catch (UsernameNotFoundException ex) {
            user = userService.createUser(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            isNewUser = true;
        }

        if (!isNewUser) {
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()
                        )
                );
            } catch (BadCredentialsException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username or password");
            }
        }

        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(DataService.escapeHtml(user.getUsername()));

        HttpStatus status = isNewUser ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}

