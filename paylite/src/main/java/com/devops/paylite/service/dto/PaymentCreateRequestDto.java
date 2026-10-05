package com.devops.paylite.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentCreateRequestDto {

    @NotBlank
    @Pattern(regexp = "\\d{16}", message = "Account must contain exactly 16 digits")
    private String account;

    @Positive
    @NotNull
    private Long amount;
}
