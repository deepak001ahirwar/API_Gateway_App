package com.deepak.app.gateway.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

        @NotNull
        private  String username;

        @NotNull
        private String password;
}
