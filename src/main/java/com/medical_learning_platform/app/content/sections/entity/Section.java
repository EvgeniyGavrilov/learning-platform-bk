package com.medical_learning_platform.app.content.sections.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("sections")
public class Section {
    @Id
    private Long id;
    private Long courseId;
    private String title;
    private int position;
}
