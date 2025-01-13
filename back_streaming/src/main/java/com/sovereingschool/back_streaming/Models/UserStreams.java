package com.sovereingschool.back_streaming.Models;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Clase para almacenar los flujos de entrada y salida de un usuario
 * 
 */
public class UserStreams {
    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;

    public UserStreams(PipedInputStream inputStream, PipedOutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public PipedInputStream getInputStream() {
        return inputStream;
    }

    public PipedOutputStream getOutputStream() {
        return outputStream;
    }
}
