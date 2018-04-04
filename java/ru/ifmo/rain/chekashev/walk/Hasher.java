package ru.ifmo.rain.chekashev.walk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class Hasher {
    private byte[] b = new byte[1024];

    public int getHash(Path file) {
        try (InputStream inputStream = Files.newInputStream(file)) {
            int x = (int) 2166136261L;
            int size;
            while ((size = inputStream.read(b)) >= 0) {
                for (int i = 0; i < size; i++) {
                    x = ((x * 16777619) ^ ((int) b[i] & 0xFF));
                }
            }
            return x;
        } catch (IOException e) {
            System.err.println("Error while reading one of the files");
            System.err.println(e.getMessage());
            return 0;
        }
    }
}
