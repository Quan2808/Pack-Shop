package com.packshop.client.dto.identity;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private Set<String> roles;

    @Override
    public String toString() {
        String rolesString = roles != null ? String.join(", ", roles) : "null";
        return "UserResponse(" +
                "userId=" + userId +
                ", username=" + username +
                ", email=" + email +
                ", fullName=" + fullName +
                ", phoneNumber=" + phoneNumber +
                ", avatarUrl=" + avatarUrl +
                ", roles=" + rolesString +
                ")";
    }
}