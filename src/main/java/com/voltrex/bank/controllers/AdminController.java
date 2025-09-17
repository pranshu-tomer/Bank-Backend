package com.voltrex.bank.controllers;

import com.voltrex.bank.dto.PendingUserDto;
import com.voltrex.bank.entities.Status;
import com.voltrex.bank.repositories.UserRepository;
import com.voltrex.bank.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/pending-users")
    public ResponseEntity<List<PendingUserDto>> getPendingUsers() {
        List<PendingUserDto> pending = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == Status.PENDING)
                .map(u -> new PendingUserDto(u.getId(), u.getFirstName(), u.getEmail(), u.getPhone()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<Map<String,Object>> approveUser(@PathVariable("id") Long id) {
        userService.approveUser(id, "admin"); // admin name could come from auth principal
        return ResponseEntity.ok(Map.of("success",true));
    }

}



//package com.voltrex.bank.controllers;
//
//import com.voltrex.bank.dto.PendingUserDto;
//import com.voltrex.bank.repositories.UserRepository;
//import com.voltrex.bank.services.UserService;
//import com.voltrex.bank.entities.User;
//import com.voltrex.bank.entities.RegistrationStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//        import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/admin")
//public class AdminController {
//
//    private final UserRepository userRepository;
//
//
//    public AdminController(UserRepository userRepository, UserService userService) {
//        this.userRepository = userRepository;
//        this.userService = userService;
//    }
//

//
//    /**
//     * Approve a pending user. This will create an Account, generate CRN + temp password,
//     * mark user as APPROVED and send an email.
//     */
//
//    /**
//     * Optional: reject user registration. Simple example.
//     */
//    @PostMapping("/users/{id}/reject")
//    public ResponseEntity<?> rejectUser(@PathVariable("id") Long id, @RequestParam(required = false) String reason) {
//        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
//        user.setStatus(RegistrationStatus.REJECTED);
//        userRepository.save(user);
//        // Optionally send rejection email via EmailService
//        return ResponseEntity.ok().build();
//    }
//}


