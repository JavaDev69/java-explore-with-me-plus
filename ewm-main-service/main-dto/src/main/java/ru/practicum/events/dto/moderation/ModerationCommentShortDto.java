package ru.practicum.events.dto.moderation;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationCommentShortDto {
    @NotBlank(message = "Comment cannot be blank")
    private String comment;
}

