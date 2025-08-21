package com.medical_learning_platform.app.content.courses.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("course_access")
public class CourseAccess {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("course_id")
    private Long courseId;

    @Column("purchased_at")
    private LocalDateTime purchasedAt;

    @Column("expires_at")
    private LocalDateTime expiresAt;
}
