package com.netflix.encodingservice.service;

import com.netflix.encodingservice.event.VideoEncodedEvent;
import com.netflix.encodingservice.event.VideoUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EncodingService {
    private final S3Client s3Client;
    private final KafkaTemplate<String, VideoEncodedEvent> kafkaTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${encoding.base-path}")
    private String basePath;

    private static final String VIDEO_ENCODED_TOPIC = "video.encoded";

    //Video qualities to encode
    //Format: resolution, bitrate(data processed per second), height
    public static final List<int[]> VIDEO_QUALITIES = Arrays.asList(
            new int[]{1920, 5000, 1080}, //1080P - 5000 Kbps(5 million bits per sec)
            new int[]{1280, 2800, 720},//720P - 2800 KbpS
            new int[]{854, 1200, 480},
            new int[]{640, 800, 360}
    );

    /*Encoding steps
    1.Download the raw video from S3 which is uploaded by video-service
    2.Encode to multiple qualities using FFMpeg processing
    3.Generate HLS playlist(.m3U8) for each quality
    4.Create the master playlist
    5.Upload all the encoded files back to S3
    6.Publish video.encoded event to KAFKA
    */

    public void encodeVideo(VideoUploadedEvent videoUploadedEvent) {
        log.info("Encoding Platform for the movie: {}", videoUploadedEvent.getMovieId());

        //Create unique path for the video
        String jobPath = basePath + "/" + videoUploadedEvent.getMovieId(); // temp/movie1
        try {
            //Create temp directories
            Files.createDirectories(Paths.get(jobPath));
            Files.createDirectories(Paths.get(jobPath + "/encoded"));  //temp/movie1/encoded

            //Step 1 - Download the raw video from S3
            String localVideoPath = jobPath + "/raw_video.mp4";     //temp/movie1/raw_video.mp4
            downloadFromS3(videoUploadedEvent.getVideoKey(), localVideoPath);
            log.info("Raw video downloaded to: {}", localVideoPath);

            //Step 2 and 3 - Encode to multiple qualities using FFMpeg processing and Generate HLS playlist
            for (int[] qualities : VIDEO_QUALITIES) {
                int width = qualities[0]; //resolution
                int bitrate = qualities[1]; //bitrate
                int height = qualities[2]; //height

                String qualityDir = jobPath + "/encoded" + height + "p";
                Files.createDirectories(Paths.get(qualityDir)); //create the dir

                encodeToHLS(localVideoPath, qualityDir, width, height, bitrate);
                log.info("Encoded {}p successfully", height); // pixels(1080p, 1920p,720p...)

                //Step4 - Generate master playlist
                String masterPlaylistPath = jobPath + "/encoded/master.m3u8";
                generateMasterPlaylist(masterPlaylistPath);
                log.info("Master playlist generated");

                //Step5 - upload all resources file to S3
                String encodedPrefix = "encoded/" + videoUploadedEvent.getMovieId() + "/"; //  encoded/movie1/
                uploadEncodedFilesToS3(jobPath + "/encoded", encodedPrefix);
                log.info("All Encoded files successfully uploaded to S3");

                //Step 6 - Publish videoEncodedEvent to KAFKA
                String masterPlaylistKey = encodedPrefix + "master.m3u8";
                String hlsURL = "https://" + bucketName + ".s3.amazonaws.com/" + masterPlaylistKey;

                VideoEncodedEvent encodedEvent = new VideoEncodedEvent(
                        videoUploadedEvent.getMovieId(),
                        hlsURL,
                        masterPlaylistKey,
                        true,
                        null
                );

                kafkaTemplate.send(VIDEO_ENCODED_TOPIC, videoUploadedEvent.getMovieId(), encodedEvent);
                log.info("VideoEncodedEvent published for the movie: {}", videoUploadedEvent.getMovieId());
            }
        } catch (Exception e) {
            log.error("Encoding failed for the movie: {}", videoUploadedEvent.getMovieId(), e.getMessage());

            //Publish the Failed Event
            VideoEncodedEvent failureEvent = new VideoEncodedEvent(
                    videoUploadedEvent.getMovieId(),
                    null,
                    null,
                    false,
                    e.getMessage()
            );
            kafkaTemplate.send(VIDEO_ENCODED_TOPIC, videoUploadedEvent.getMovieId(), failureEvent);
        }
        finally{
            //cleanup temp files
            cleanUpTempFiles(jobPath);
        }
    }
}
