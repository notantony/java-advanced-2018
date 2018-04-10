package ru.ifmo.rain.chekashev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class Implementor implements JarImpler {
    private Class<?> token;
    private Writer writer;

    private static final String ENDL = System.lineSeparator(),
            NEWLINE = ";" + ENDL,
            TAB = "   ";


    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (root == null) {
            throw new ImplerException("Path to root cannot be null");
        }

        this.token = token;


        root = Paths.get("").toAbsolutePath().resolve(root.toString());
        Path path = root.resolve(token.getPackageName().replace('.', File.separatorChar));

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new ImplerException("Error occurred while creating directory for output files");
            }
        }

        try {
            writer = Files.newBufferedWriter(path.resolve(token.getSimpleName() + ".java"), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new ImplerException("Error occurred while creating/opening output file");
        }

        try {
            addPackage();
            addName();
            addSuper();
            addInterfaces();
            writer.write(" {");

            writer.write(Modifier.toString(token.getModifiers()));
            addInterfaces();
        } catch (IOException e) {
            throw new ImplerException("Error while writing output .java file");
        }
    }

    private void addPackage() throws IOException {
        writer.write(token.getPackage().toString());
        writer.write(NEWLINE);
    }

    private void addName() throws IOException {
        writer.write(token.isInterface() ? " interface " : " class ");
        writer.write(token.getSimpleName());
        writer.write("Impl");
    }

    private void addSuper() throws IOException {
        Class tmp = token.getSuperclass();
        if (tmp != null) {
            writer.write(" extends ");
            writer.write(tmp.getSimpleName());
        }
    }

    private void addInterfaces() throws IOException {
        Class<?>[] tmp = token.getInterfaces();
        writer.write(" implements");
        boolean flag = false;
        for (Class<?> one : tmp) {
            if (!flag) {
                flag = true;
            } else {
                writer.write(",");
            }
            writer.write(" ");
            writer.write(one.getSimpleName());
        }
    }

    //private void addExceptions() throws IOException {
    //    Class<?>[] tmp = token.;

//    }

    @Override
    public void implementJar(Class<?> token, Path root) throws ImplerException {

    }

}
