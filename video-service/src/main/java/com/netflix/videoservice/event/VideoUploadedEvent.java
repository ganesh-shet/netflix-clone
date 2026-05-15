package com.netflix.videoservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//When a video is uploaded to S3, event will be triggered to kafka and
// encoding service consume this to start FFMPEG processing
@Data @NoArgsConstructor @AllArgsConstructor
public class VideoUploadedEvent {
    private String movieId;
    private String videoKey;
    private String bucketName;
    private String originalFileName;
    private Long fileSizeBytes;
}
