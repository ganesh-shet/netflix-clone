package com.netflix.contentservice.controller;

import com.netflix.contentservice.dto.MovieRequest;
import com.netflix.contentservice.dto.MovieResponse;
import com.netflix.contentservice.model.Genre;
import com.netflix.contentservice.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@Slf4j
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    //Add new movie to catalog
    @PostMapping
    public ResponseEntity<MovieResponse> addMovie(@Valid @RequestBody MovieRequest movieRequest) {
        MovieResponse newMovie  = contentService.addMovie(movieRequest);
        return new ResponseEntity<>(newMovie, HttpStatus.CREATED);
    }

    //Get All the movies
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        List<MovieResponse> movies = contentService.getAllMovies();
        return  new ResponseEntity<>(movies,HttpStatus.OK);
    }

    //Get movie by ID
    @GetMapping("/{movieId}")
    private ResponseEntity<MovieResponse> getMovieById(@PathVariable String movieId) {
        MovieResponse movie = contentService.getMovieById(movieId);
        return new ResponseEntity<>(movie,HttpStatus.OK);
    }


    //Get movies by Genre
    @GetMapping("/genre/{genre}")
    private ResponseEntity<List<MovieResponse>> getMoviesByGenre(@PathVariable Genre genre) {
        List<MovieResponse> movie = contentService.getMoviesByGenre(genre);
        return new ResponseEntity<>(movie,HttpStatus.OK);
    }

    //Search movies
    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(@RequestParam String title){
        List<MovieResponse> searchedMovies = contentService.searchMovies(title);
        return new ResponseEntity<>(searchedMovies,HttpStatus.OK);

    }
}
