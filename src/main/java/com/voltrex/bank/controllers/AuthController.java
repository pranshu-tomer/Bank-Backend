package com.voltrex.bank.controllers;

import com.voltrex.bank.dto.LoginRequest;
import com.voltrex.bank.dto.PasswordChangeRequest;
import com.voltrex.bank.dto.RegisterRequest;
import com.voltrex.bank.entities.Status;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.services.AuthService;
import com.voltrex.bank.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String,Object>> register(@Valid @RequestBody RegisterRequest request) {
        User savedUser = authService.register(request);
        return ResponseEntity.ok(Map.of("success",true,"userId",savedUser.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }
}


//package com.voltrex.bank.controllers;
//
//import com.voltrex.bank.dto.LoginRequest;
//import com.voltrex.bank.dto.RegisterRequest;
//import com.voltrex.bank.dto.RegisterResponse;
//import com.voltrex.bank.entities.User;
//import com.voltrex.bank.services.AuthService;
//import com.voltrex.bank.services.UserService;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.validation.Valid;
//
//import java.util.Map;
//
////@CrossOrigin(origins = "*")
//@RestController
//@RequestMapping("/api/auth")
//@Validated
//public class AuthController {
//
//    private final UserService userService;
//    private final AuthService authService;
//
//    public AuthController(UserService userService, AuthService authService) {
//        this.userService = userService;
//        this.authService = authService;
//    }
//

//}
//
