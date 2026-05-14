package com.netflix.contentservice.dto;

import com.netflix.contentservice.model.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Genre is required")
    private Genre genre;

    private String director;
    private String cast;
    private String releaseDate;
    private String rating;
    private String thumbnailUrl;
    private String durationMinutes;
}
