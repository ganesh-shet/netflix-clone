package com.netflix.contentservice.service;

import com.netflix.contentservice.dto.MovieRequest;
import com.netflix.contentservice.dto.MovieResponse;
import com.netflix.contentservice.model.Genre;
import com.netflix.contentservice.model.Movie;
import com.netflix.contentservice.model.VideoStatus;
import com.netflix.contentservice.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentService {
    private final MovieRepository movieRepository;

    public MovieResponse addMovie(MovieRequest movieRequest){
        log.info("Adding a new movie");

        Movie movie = new Movie();
        movie.setTitle(movieRequest.getTitle());
        movie.setDescription(movieRequest.getDescription());
        movie.setGenre(movieRequest.getGenre());
        movie.setDirector(movieRequest.getDirector());
        movie.setCast(movieRequest.getCast());
        movie.setReleaseDate(movieRequest.getReleaseDate());
        movie.setRating(movieRequest.getRating());
        movie.setThumbnailUrl(movieRequest.getThumbnailUrl());
        movie.setDurationMinutes(movieRequest.getDurationMinutes());
        movie.setVideoStatus(VideoStatus.PENDING);

        Movie savedMovie = movieRepository.save(movie);
        log.info("Movie added successfully with ID: {}", savedMovie.getId());

        //Returning the Movie Response
        return mapToResponse(savedMovie);
    }

    //Convert movie to Movie Response
    private MovieResponse mapToResponse(Movie movie){
        MovieResponse movieResponse = new MovieResponse();
        movieResponse.setId(movie.getId());
        movieResponse.setTitle(movie.getTitle());
        movieResponse.setDescription(movie.getDescription());
        movieResponse.setGenre(movie.getGenre());
        movieResponse.setDirector(movie.getDirector());
        movieResponse.setCast(movie.getCast());
        movieResponse.setReleaseDate(movie.getReleaseDate());
        movieResponse.setRating(movie.getRating());
        movieResponse.setThumbnailUrl(movie.getThumbnailUrl());
        movieResponse.setDurationMinutes(movie.getDurationMinutes());
        movieResponse.setVideoStatus(movie.getVideoStatus());
        movieResponse.setVideoKey(movie.getVideoKey());
        movieResponse.setHlsUrl(movie.getHlsUrl());
        movieResponse.setCreatedAt(movie.getCreatedAt());

        return movieResponse;
    }

    public List<MovieResponse> getAllMovies(){
        return movieRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MovieResponse getMovieById(String movieId){
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->new RuntimeException("Movie not found" + movieId));
        return mapToResponse(movie);
    }

    public List<MovieResponse> getMoviesByGenre(Genre genre){
        return movieRepository.findByGenre(genre)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    public List<MovieResponse> searchMovies(String title){
        return  movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void updateVideoKey(String movieId, String videoKey){
        log.info("Updating videokey for movie: {}", movieId);
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->new RuntimeException("Movie not found" + movieId));
        movie.setVideoKey(videoKey);
        movie.setVideoStatus(VideoStatus.UPLOADED);
        movieRepository.save(movie);
    }

    public void updateHlsUrl(String movieId, String hlsUrl){
        log.info("Updating hlsurl for movie: {}", movieId);
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->new RuntimeException("Movie not found" + movieId));
        movie.setHlsUrl(hlsUrl);
        movie.setVideoStatus(VideoStatus.READY);

        movieRepository.save(movie);

        log.info("Movie is ready for streaming", movieId);
    }

}
