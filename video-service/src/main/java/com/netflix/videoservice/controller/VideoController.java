package com.netflix.videoservice.controller;

import com.netflix.videoservice.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/api/v1/Videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    //Upload video file for a movie
    //accepts multipart file upload
    @PostMapping("/upload/{movieId}")
    public ResponseEntity<String> uploadVideo
                            (@PathVariable String movieId, @RequestParam("file")MultipartFile file) throws IOException {
        log.info("Video upload request for movie: {}MB", movieId,file.getSize() / (1024*1024));
        if(file.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty file");
        }
        String videoKey = videoService.uploadVideo(movieId, file);

        return ResponseEntity.ok("Video uploaded successfully" +videoKey + "- Encoding started automatically via kafka");
    }

}
