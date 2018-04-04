package ru.ifmo.rain.chekashev.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {

    private static String ERROR_MESSAGE_WRITING = "Error occurred while writing to output file";

    private static Path makeDir(String pathString) throws SecurityException, IOException {
        Path path = Paths.get(pathString);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        return path;
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Wrong arguments format, use: <input filename> <output filename>");
            return;
        }

        try (BufferedReader inputReader =
                     Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8)) {
            try (Writer outputWriter =
                         Files.newBufferedWriter(makeDir(args[1]), StandardCharsets.UTF_8)) {
                String pathString;
                while ((pathString = inputReader.readLine()) != null) {
                    try {
                        Path one = Paths.get(pathString);
                        Files.walkFileTree(one, new SimpleFileVisitor<Path>() {
                            private final Hasher hasher = new Hasher();

                            @Override
                            public FileVisitResult visitFile(Path filePath, BasicFileAttributes bfa) throws IOException {
                                outputWriter.write(String.format("%08x %s%n", hasher.getHash(filePath), filePath.toString()));
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path filePath, IOException e) throws IOException {
                                System.err.println("Error occurred while visiting one of the files");
                                System.err.println(e.getMessage());
                                outputWriter.write(String.format("00000000 %s%n", filePath.toString()));
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (InvalidPathException e) {
                        System.err.println(e.getMessage());
                        outputWriter.write(String.format("00000000 %s%n", pathString));
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.err.println(ERROR_MESSAGE_WRITING);
                    }
                }
            } catch (InvalidPathException e) {
                System.err.println("Invalid path to input file");
                System.err.println(e.getMessage());
            } catch (SecurityException e) {
                System.err.println("Cannot access input file");
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println("Error occurred while reading from input file");//Files.newBufferedReader doesn't throw FileNotFoundException?
                System.err.println(e.getMessage());
            }
        } catch (SecurityException e) {
            System.err.println("Cannot create output file: permission denied");
            System.err.println(e.getMessage());
        } catch (InvalidPathException e) {
            System.err.println("Invalid path to output file");
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(ERROR_MESSAGE_WRITING);
            System.err.println(e.getMessage());
        }
    }
}
