package com.sovereingschool.back_streaming.Models;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Clase para almacenar los flujos de entrada y salida de un usuario
 * 
 */
public class UserStreams {

    private PipedInputStream ffprobeInputStream;
    private PipedOutputStream ffprobeOutputStream;
    private PipedInputStream ffmpegInputStream;
    private PipedOutputStream ffmpegOutputStream;

    public UserStreams(PipedInputStream ffprobeinputStream, PipedOutputStream ffprobeoutputStream,
            PipedInputStream ffmpeginputStream, PipedOutputStream ffmpegoutputStream) {
        this.ffprobeInputStream = ffprobeinputStream;
        this.ffprobeOutputStream = ffprobeoutputStream;
        this.ffmpegInputStream = ffmpeginputStream;
        this.ffmpegOutputStream = ffmpegoutputStream;
    }

    public PipedInputStream getFFprobeInputStream() {
        return ffprobeInputStream;
    }

    public PipedOutputStream getFFprobeOutputStream() {
        return ffprobeOutputStream;
    }

    public PipedInputStream getFFmpegInputStream() {
        return ffmpegInputStream;
    }

    public PipedOutputStream getFFmpegOutputStream() {
        return ffmpegOutputStream;
    }
}
