package com.example.chatonlinedemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
    
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatar;
    
    @Size(max = 500, message = "Facebook link must not exceed 500 characters")
    private String facebookLink;
}
