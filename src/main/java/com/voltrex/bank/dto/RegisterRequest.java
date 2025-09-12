package com.voltrex.bank.dto;

import com.voltrex.bank.entities.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank
    private String firstName;

    private String lastName;

    @NotBlank @Email
    private String email;

    @NotBlank
    // simple phone validation; adapt to your locale
    @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "Invalid phone number")
    private String phone;

    @DateTimeFormat
    private LocalDate dob;


    @NotBlank
    private String street;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String pincode;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull
    @PositiveOrZero
    private Integer age;
}

