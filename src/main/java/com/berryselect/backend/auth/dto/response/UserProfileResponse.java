package com.berryselect.backend.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private String phone;
    private LocalDate birth;
}
