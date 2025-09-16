package com.voltrex.bank.services;

import com.voltrex.bank.dto.LoginRequest;
import com.voltrex.bank.dto.RegisterRequest;
import com.voltrex.bank.entities.Address;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.entities.Status;
import com.voltrex.bank.exception.EmailAlreadyExistsException;
import com.voltrex.bank.exception.PhoneAlreadyExistsException;
import com.voltrex.bank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException("Phone Number already exists");
        }

        // Build user entity directly
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(Address.builder()
                        .street(request.getStreet())
                        .city(request.getCity())
                        .state(request.getState())
                        .pincode(request.getPincode())
                        .build())
                .dob(request.getDob())
                .age(request.getAge())
                .gender(request.getGender())
                .twoFactorEnabled(false)
                .build();

        user.setStatus(Status.PENDING);
        return userRepository.save(user);
    }

    public ResponseEntity<Map<String,Object>> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getCrn(), loginRequest.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(Map.of("success", true, "token", token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Invalid credentials"));
        }
    }
}





//package com.voltrex.bank.services;
//
//import com.voltrex.bank.dto.LoginRequest;
//import com.voltrex.bank.entities.User;
//import org.springframework.security.authentication.AuthenticationManager;

//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AuthService {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtService jwtService;
//
//    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
//        this.authenticationManager = authenticationManager;
//        this.jwtService = jwtService;
//    }
//

//}
