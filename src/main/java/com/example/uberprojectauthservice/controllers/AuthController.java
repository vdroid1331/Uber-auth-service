package com.example.uberprojectauthservice.controllers;


import com.example.uberprojectauthservice.dto.AuthRequestDto;
import com.example.uberprojectauthservice.dto.AuthResponseDto;
import com.example.uberprojectauthservice.dto.PassengerDto;
import com.example.uberprojectauthservice.dto.PassengerSignupRequestDto;
import com.example.uberprojectauthservice.services.AuthService;
import com.example.uberprojectauthservice.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Value("${cookie.expiry}")
    private int cookieExpiry;

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup/passenger")
    public ResponseEntity<PassengerDto> signUp(@RequestBody PassengerSignupRequestDto passengerSignupRequestDto) {
        PassengerDto response = authService.signupPassenger(passengerSignupRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signin/passenger")
    public ResponseEntity<?> signIn(@RequestBody AuthRequestDto authRequestDto, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword()));
        if (authentication.isAuthenticated()) {
            String jwtToken = jwtService.createToken(authRequestDto.getEmail());
            ResponseCookie cookie = ResponseCookie.from("JwtToken", jwtToken)
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .maxAge(cookieExpiry)
                            .build();
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return new ResponseEntity<>(AuthResponseDto.builder().success(true).build(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Auth Not Successful", HttpStatus.OK);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(HttpServletRequest request) {
        for (Cookie cookie: request.getCookies()) {
            if (cookie.getName().equals("JwtToken")) {}
        }
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

}
