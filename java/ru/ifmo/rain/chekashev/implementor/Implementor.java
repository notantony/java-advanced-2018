package ru.ifmo.rain.chekashev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements JarImpler {
    private Class<?> token;

    private static final String NEWLINE = ";\n";

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (root == null) {
            throw new ImplerException("Path to root cannot be null");
        }
        root =
        Writer writer = new Files.newBufferedWriter(root, );

        this.token = token;
        Paths.get(root);
        s.append(token.getPackage());
        s.append(NEWLINE);
        s.append(Modifier.toString(token.getModifiers()));
        s.append(token.isInterface() ? " interface " : " class ");
        s.append(token.getSimpleName() + "Impl");
        addSuper();
        addInterfaces();
        System.out.println(s);
    }

    private void addSuper() {
        Class tmp = token.getSuperclass();
        if (tmp != null) {
            s.append(" extends ");
            s.append(tmp.getSimpleName());
        }
    }

    private void addInterfaces() {
        Class<?>[] tmp = token.getInterfaces();
        s.append(" implements");
        boolean flag = false;
        for (Class<?> one : tmp) {
            if (!flag) {
                flag = true;
            } else {
                s.append(",");
            }
            s.append(" ");
            s.append(one.getSimpleName());
        }
    }


}
