package com.medical_learning_platform.app.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessInfo {
    private boolean isAuthor;
    private boolean editAccess; // автор = true, остальные = false (пока так)
    private boolean readAccess; // автор = true, либо подписчик = true, иначе false
}
