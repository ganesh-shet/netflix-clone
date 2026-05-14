package com.netflix.contentservice.model;

/*
Tracks the video processing lifecycle

FLOW:
PENDING -> UPLOADING -> UPLOADED -> ENCODING -> READY
                                               OR FAILED
 */
public enum VideoStatus {
    PENDING,  //Movie added but not uploaded yet
    UPLOADED, //Raw video uploaded to S3
    ENCODING, //FFmpeg encoding the video
    ENCODED,  //Encoding completed
    READY,    //HLS playlist ready and can be streamed
    FAILED    //Encoding failed
}
