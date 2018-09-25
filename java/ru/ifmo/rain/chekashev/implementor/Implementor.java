package ru.ifmo.rain.chekashev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

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
     * Return default value with leading space for passed <tt>token</tt> conversed to string.
     * <p>
     * Return " 0" for primitives, " false" for boolean and " null" for object references.
     * If passed class is null, empty string returned.
     *
     * @param token class to get default value
     * @return class default value string with single space.
     */
    private String getDefaultValueString(Class token) {
        if (token.equals(boolean.class)) {
            return SPACE + "false";
        } else if (token.equals(void.class)) {
            return "";
        } else if (token.isPrimitive()) {
            return SPACE + "0";
        }
        return SPACE + "null";
    }

    /**
     * Returns modifier without <tt>transient</tt> and <tt>abstract</tt> tags converted to string.
     *
     * @param modifier modifier to be converted
     * @return string which consists of all other modifiers
     */
    private String cleanModifiers(int modifier) {
        return Modifier.toString(modifier & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT);
    }

    /**
     * Removes all implemented methods of <tt>token</tt> from passed {@link = java.lang.util.Set} set.
     *
     * @param token passed class
     * @param set   passed set
     */
    private void clearMethodsClass(Class token, Set<Method> set) {
        for (Method one : token.getDeclaredMethods()) {
            if (!Modifier.isAbstract(one.getModifiers())) {
                set.remove(one);
            }
        }
        if (token.getSuperclass() != null) {
            clearMethodsClass(token.getSuperclass(), set);
        }
    }

    /**
     * Checks if any of passed parameters is incorrect or <tt>null</tt>, otherwise an exception is thrown.
     *
     * @param token first parameter
     * @param root  second parameter
     * @throws ImplerException is thrown any parameter is <tt>null</tt> or class cannot be implemented
     */
    private void checkParameters(Class<?> token, Path root) throws ImplerException {
        if (token == null) {
            throw new ImplerException("Class token cannot be null");
        }
        if (root == null) {
            throw new ImplerException("Path to root cannot be null");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Passed class cannot be final");
        }
        if (token.isArray()) {
            throw new ImplerException("Passed class cannot be an array");
        }
        if (token.equals(Enum.class)) {
            throw new ImplerException("Passed class cannot be enum");
        }
        if (token.isPrimitive()) {
            throw new ImplerException("Passed class cannot be primitive");
        }
    }

    /**
     * Writes package info.
     * <p>
     * Writes information about package of the stored class <tt>token</tt> in standard format to the corresponding java file created for the class using stored <tt>writer</tt>.
     *
     * @throws IOException if error occurs while writing
     */
    private void addPackage() throws IOException {
        writer.write(token.getPackage().toString());
    }

    /**
     * Writes class heading.
     * <p>
     * Writes information about the stored class <tt>token</tt> using stored <tt>writer</tt>.
     * Generated class extends <tt>token</tt> class and named same but with <tt>"Impl"</tt> prefix.
     *
     * @throws IOException if error occurs while writing
     */
    private void addHeading() throws IOException {
        writer.write(cleanModifiers(token.getModifiers() & ~Modifier.INTERFACE) +
                SPACE + "class" +
                SPACE + token.getSimpleName() + "Impl");
        writer.write((token.isInterface() ? " implements " : " extends ") + token.getCanonicalName());
    }

    /**
     * Implements methods that will return default values.
     * <p>
     * Writes an implementation for abstract and interface methods of the stored class <tt>token</tt> in the java file using stored <tt>writer</tt>.
     * Methods that have been already implemented in ancestors will be added.
     *
     * @throws IOException if error occurs while writing
     * @see #getDefaultValueString(Class)
     * @see #clearMethodsClass(Class, Set)
     */
    private void implementMethods() throws IOException {
        HashSet<Method> set = new HashSet<>(Arrays.asList(token.getMethods()));
        set.addAll(Arrays.asList(token.getDeclaredMethods()));
        clearMethodsClass(token, set);
        for (Method one : set) {
            writer.write(TAB + cleanModifiers(one.getModifiers()) + SPACE +
                    one.getReturnType().getCanonicalName() + SPACE + one.getName());
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
                    TAB + TAB + "return" +
                    getDefaultValueString(one.getReturnType()) + NEWLINE +
                    TAB + RBRACE + ENDL + ENDL);
        }
    }

    /**
     * Writes default implementation of constructors.
     * <t>
     * Writes non-private constructors of the stored class <tt>token</tt> in the java file using stored <tt>writer</tt>.
     * Implemented constructors do nothing but call super-constructors with same arguments.
     *
     * @return <tt>true</tt> if at least one constructor has been implemented
     * @throws IOException if error occurs while writing
     */
    private boolean implementConstructors() throws IOException {
        boolean any = false;
        for (Constructor one : token.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(one.getModifiers())) {
                any = true;
                writer.write(TAB + cleanModifiers(one.getModifiers()) + SPACE +
                        token.getSimpleName() + "Impl");
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
        return any;
    }

    /**
     * Writes <tt>args</tt> names in line.
     * <p>
     * Writes <tt>args</tt> names in line divided by commas and spaces to the java file using stored <tt>writer</tt>.
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


    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     *
     * @param token type token to create implementation for.
     * @param root  target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.
     * @see #implement(Class, Path)
     */
    @Override
    public void implementJar(Class<?> token, Path root) throws ImplerException {
        checkParameters(token, root);

        this.token = token;

        root = Paths.get("").toAbsolutePath().resolve(root.toString());//TODO: copypaste
        Path path = root.resolve(token.getPackageName().replace('.', File.separatorChar));

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new ImplerException("Cannot create output directory", e);
            }
        }

        try {
            implement(token, path);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            String[] args = new String[]{
                    "-cp",
                    path.toString() + File.pathSeparator + System.getProperty("java.class.path"),
            };
            if (compiler == null || compiler.run(null, null, null, args) != 0) {
                throw new ImplerException("Unable to compile generated files");
            }
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VENDOR, "Chekashev Anton");
            try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(path), manifest)) {
                writer.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
                //Files.copy(getFilePath(tempDir, token, ), writer);
            } catch (IOException e) {
                throw new ImplerException("Unable to write to JAR file", e);
            }
        } finally {
            /*
            try {
                clean(tempDir);
            } catch (IOException e) {
                System.out.println("Unable to delete temp directory: " + e.getMessage());
            }
            */
        }
    }


    /**
     * Produces java file for class with default implementation.
     * <p>
     * Produces java file for <tt>token</tt> in passed path <tt>root</tt> with default implementation.
     * <tt>token</tt> cannot be generic, abstract, array or final.
     * Passed class should have at least one non-private constructor if it is not an interface.
     *
     * @param token class token to create implementation for.
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
                throw new ImplerException("Cannot create output directory", e);
            }
        }

        try (Writer w = Files.newBufferedWriter(path.resolve(token.getSimpleName() + "Impl.java"), StandardCharsets.US_ASCII)) {
            writer = w;
            addPackage();
            writer.write(NEWLINE + ENDL);
            addHeading();
            writer.write(SPACE + LBRACE + ENDL);
            if (!implementConstructors() && !token.isInterface()) {
                throw new ImplerException("Class should have at least one constructor");
            }
            implementMethods();
            writer.write(RBRACE);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new ImplerException("Error while writing java file", e);
        }
    }


    /**
     * Runs implementor in specified mode with arguments passed with <tt>args</tt>.
     * Arguments format: <tt>[-jar] <Class> <Path> </tt>.
     * use <tt>-jar</tt> parameter for implementing jar file.
     * If an error occurs implementing is stopped and error is shown on the screen.
     *
     * @param args arguments passed
     * @see #implement(Class, Path)
     * @see #implementJar(Class, Path)
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
            System.out.println("Error: invalid path " + e.getMessage());
        } catch (ImplerException e) {
            System.out.println("Error occurred while implementing: " + e.getMessage());
        }
    }

}