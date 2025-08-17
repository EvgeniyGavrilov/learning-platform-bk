package com.medical_learning_platform.app.auth.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("code")
public class Code {
    @Id
    private Long id;
    private String email;
    private String hash;
    private LocalDateTime expiresAt;
}
