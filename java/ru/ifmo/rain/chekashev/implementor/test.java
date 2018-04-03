package ru.ifmo.rain.chekashev.implementor;

import java.nio.file.Path;
import java.nio.file.Paths;

public class test {
    public static void main(String[] args) {
        Implementor one = new Implementor();
        one.implement(String.class, null);
    }
}
