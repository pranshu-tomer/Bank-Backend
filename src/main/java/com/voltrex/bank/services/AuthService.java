package com.voltrex.bank.services;

import com.voltrex.bank.dto.RegisterRequest;
import com.voltrex.bank.entities.Address;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.entities.Status;
import com.voltrex.bank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User register(RegisterRequest request) {
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
                .build();

        return userRepository.save(user);
    }
}





//package com.voltrex.bank.services;
//
//import com.voltrex.bank.dto.LoginRequest;
//import com.voltrex.bank.entities.User;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
//    public String login(LoginRequest loginRequest) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(loginRequest.getCrn(),loginRequest.getPassword())
//        );
//        User user = (User)authentication.getPrincipal();
//        return jwtService.generateToken(user);
//    }
//}
