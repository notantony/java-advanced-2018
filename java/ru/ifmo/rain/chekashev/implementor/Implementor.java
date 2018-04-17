package ru.ifmo.rain.chekashev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class Implementor implements JarImpler {
    private Class<?> token;
    private Writer writer;

    private static final String ENDL = System.lineSeparator(),
            NEWLINE = ";" + ENDL,
            SPACE = " ",
            TAB = "    ",
            COMMA = ",",
            LBRACE = "{",
            RBRACE = "}",
            LBRACKET = "(",
            RBRACKET = ")",
            ARG = "arg";


    /**
     * Checks if any of the passed parameters is null, otherwise an exception is thrown.
     * @param token first parameter
     * @param root second parameter
     * @throws ImplerException is thrown any parameter is null
     */
    private void checkParameters(Class<?> token, Path root) throws ImplerException {
        if (token == null) {
            throw new ImplerException("Class token cannot be null");
        }
        if (root == null) {
            throw new ImplerException("Path to root cannot be null");
        }
    }

    /**
     * Creates java file for class with default implementation.
     * Creates java file for <tt>token</tt> in passed path <tt>root</tt> with default implementation.
     * <tt>token</tt> cannot be generic, abstract, array or final
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException is thrown if an error occurs during implementation
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkParameters(token, root);

        this.token = token;


        root = Paths.get("").toAbsolutePath().resolve(root.toString());
        Path path = root.resolve(token.getPackageName().replace('.', File.separatorChar));

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new ImplerException("Error while creating directory for output java file", e);
            }
        }

        try {
            writer = Files.newBufferedWriter(path.resolve(token.getSimpleName() + ".java"), StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new ImplerException("Error while creating/opening output java file", e);
        }

        try {
            addPackage();
            writer.write(NEWLINE + ENDL);
            addName();
            writer.write(SPACE + LBRACE + ENDL);
            addConstructors();
            addFunctions();
            writer.write(RBRACE);

            writer.close();
        } catch (IOException e) {
            try {
                writer.close();
            } catch (IOException ee) {
            }
            throw new ImplerException("Error while writing output java file", e);
        }
    }

    /**
     * Writes package info.
     * Writes information about package of the stored class <tt>token</tt> in standard format to the corresponding java file created for the class using stored <tt>writer</tt>.
     *
     * @throws IOException if error occurs while writing
     */
    private void addPackage() throws IOException {
        writer.write(token.getPackage().toString());
    }

    /**
     * Writes class heading.
     * Writes information about the stored class <tt>token</tt> with modifiers in standard format to the corresponding java file created for the class using stored <tt>writer</tt>.
     * Adds information about modifiers, class name, super class(if it exists) ans implemented interfaces.
     *
     * @throws IOException if error occurs while writing
     */
    private void addName() throws IOException {//TODO: refactor spaces
        writer.write(Modifier.toString(token.getModifiers()) +
                SPACE + (token.isInterface() ? "interface" : "class") +
                SPACE + token.getSimpleName() + "Impl");


        Class tmp = token.getSuperclass();
        if (tmp != null) {
            writer.write(" extends " +
                    tmp.getCanonicalName());
        }

        writer.write(" implements");
        boolean flag = false;
        for (Class<?> one : token.getInterfaces()) {
            if (!flag) {
                flag = true;
            } else {
                writer.write(COMMA);
            }
            writer.write(SPACE + one.getCanonicalName());
        }
    }

    /**
     * Writes methods that return default values.
     * Implements methods of the stored class <tt>token</tt> in the corresponding java file using stored <tt>writer</tt>.
     * Implemented methods return default values.
     *
     * @throws IOException if error occurs while writing
     */
    private void addFunctions() throws IOException {//TODO: abstract methods
        for (Method one : token.getMethods()) {
            writer.write(TAB + Modifier.toString(one.getModifiers()) + SPACE +
                    one.getReturnType().getCanonicalName() + SPACE + one.getName());
            writer.write(LBRACKET);
            addArgs(one.getParameterTypes(), false);
            writer.write(RBRACKET + SPACE);
            Class<?>[] tmp = one.getExceptionTypes();
            if (tmp.length != 0) {
                writer.write("throws" + SPACE);
                addArgs(tmp, false);
                writer.write(SPACE);
            }
            writer.write(LBRACE + ENDL +
                    TAB + TAB + "return" + SPACE +
                    (one.getReturnType().isPrimitive() ? "0" : "null") + NEWLINE +
                    TAB + RBRACE + ENDL + ENDL);
        }
    }


    /**
     * Writes constructors calling super class constructors.
     * Implements constructors of the stored class <tt>token</tt> in the corresponding java file using stored <tt>writer</tt>.
     * Implemented methods return default values.
     *
     * @throws IOException if error occurs while writing
     */
    private void addConstructors() throws IOException {
        for (Constructor one : token.getConstructors()) {
            writer.write(TAB + Modifier.toString(one.getModifiers()) + SPACE +
                    token.getSimpleName());
            writer.write(LBRACKET);
            addArgs(one.getParameterTypes(), true);
            writer.write(RBRACKET + SPACE);
            Class<?>[] tmp = one.getExceptionTypes();
            if (tmp.length != 0) {
                writer.write("throws" + SPACE);
                addArgs(tmp, false);
                writer.write(SPACE);
            }
            writer.write(LBRACE + ENDL +
                    TAB + TAB + "super" + LBRACKET);
            boolean flag = false;
            for (int i = 0; i < one.getParameterCount(); i++) {
                if (!flag) {
                    flag = true;
                } else {
                    writer.write(COMMA + SPACE);
                }
                writer.write(ARG + i);
            }
            writer.write(RBRACKET + NEWLINE + TAB + RBRACE + ENDL + ENDL);
        }
    }

    /**
     * Writes <tt>args</tt> names in line.
     * Writes <tt>args</tt> names in line divided by commas and spaces to the corresponding java file using stored <tt>writer</tt>.
     * If printNames is <tt>true</tt> also adds names <tt>arg1, arg2, arg3, ...</tt> after class names.
     *
     * @param args       array of classes which names should be written
     * @param printNames controls writing names after class names
     * @throws IOException if error occurs while writing
     */
    private void addArgs(Class<?>[] args, boolean printNames) throws IOException {
        boolean flag = false;
        int num = 0;
        for (Class<?> one : args) {
            if (!flag) {
                flag = true;
            } else {
                writer.write(COMMA + SPACE);
            }
            writer.write(one.getCanonicalName());
            if (printNames) {
                writer.write(SPACE + ARG + num++);
            }
        }
    }


    public void implementJar(Class<?> token, Path path) throws ImplerException {

    }
    /*
    /** Creates java file for <tt>token</tt> class
     * implemented with
     * @param token
     * @param path
     * @throws ImplerException
     */
    /*
    @Override
    public void implementJar(Class<?> token, Path path) throws ImplerException {
        checkParameters(token, path);
       // makeDir(path);
        Path tempDir;
        try {
         //   tempDir = Files.createTempDirectory(outputFile.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Unable to create temp directory", e);
        }
        try {
            implement(token, tempDir);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            String[] args = new String[]{
                    "-cp",
                    tempDir.toString() + File.pathSeparator + System.getProperty("java.class.path"),
                    getFilePath(tempDir, token, JAVA).toString()
            };
            if (compiler == null || compiler.run(null, null, null, args) != 0) {
                throw new ImplerException("Unable to compile generated files");
            }
            Manifest manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Chekashev Anton");
            try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(outputFile), manifest)) {
                writer.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
                Files.copy(getFilePath(tempDir, token, ), writer);
            } catch (IOException e) {
                throw new ImplerException("Unable to write to JAR file", e);
            }
        } finally {
            try {
                clean(tempDir);
            } catch (IOException e) {
                System.out.println("Unable to delete temp directory: " + e.getMessage());
            }
        }
    }
    */

    /**
     * Runs implementor in specified mode with arguments passed with <tt>args</tt>.
     * Arguments format: <tt>[-jar] <Class> <Path> </tt>.
     * use <tt>-jar</tt> parameter for implementing jar file.
     * If an error occurs implementing is stopped and error is shown on the screen.
     *
     * @param args arguments passed
     */
    public static void main(String[] args) {
        if (args == null) {
            System.out.println("Error: args[] cannot be null");
            return;
        }
        if ((args.length != 2 && args.length != 3) || args.length == 3 && !args[0].equals("-jar")) {
            System.out.println("Error: wrong format, use: [-jar] <Class> <Path>");
            return;
        }
        for (String one : args) {
            if (one == null) {
                System.out.println("Error: none of arguments can be null");
            }
        }


        JarImpler implementor = new Implementor();
        try {
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error: invalid class " + e.getMessage());
        } catch (InvalidPathException e) {
            System.out.println("Error Invalid path " + e.getMessage());
        } catch (ImplerException e) {
            System.out.println("Error occurred while implementing: " + e.getMessage());
        }
    }

}
