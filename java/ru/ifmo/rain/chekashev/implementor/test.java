package ru.ifmo.rain.chekashev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class test {
    public static void main(String[] args) throws ImplerException {
        Implementor one = new Implementor();
        one.implement(String.class, Paths.get("root/"));
    }
}
