
package ru.ifmo.rain.chekashev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

public class test {
    public static void main(String[] args) throws ImplerException {
        Implementor one = new Implementor();
        one.implement(javax.annotation.processing.Completions.class, Paths.get("root/"));
    }
}

