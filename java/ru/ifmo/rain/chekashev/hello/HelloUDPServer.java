package ru.ifmo.rain.chekashev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService pool;
    private boolean isRunning;

    public HelloUDPServer() {
        isRunning = false;
    }

    @Override
    public void start(int port, int threads) {
        if (isRunning) {
            System.out.println("Server is already running.");
            return;
        }
        isRunning = true;
        final int bufferSize;
        try {
            socket = new DatagramSocket(port);
            bufferSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            System.err.println("Cannot create server socket " + e.getMessage());
            return;
        }
        System.out.println("Server started at " + socket.getInetAddress());
        threads++;
        pool = new ThreadPoolExecutor(
                threads,
                threads,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(threads),
                new ThreadPoolExecutor.DiscardPolicy()
        );
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    DatagramPacket get = new DatagramPacket(new byte[bufferSize], bufferSize);
                    try {
                        socket.receive(get);
                    } catch (IOException e) {
                        System.err.println("Error occurred while receiving message " + e.getMessage());//TODO
                    }
                    pool.submit(() -> {
                        String outMessage = "Hello, " + new String(get.getData(), get.getOffset(), get.getLength(), StandardCharsets.UTF_8);
                        DatagramPacket post = new DatagramPacket(
                                outMessage.getBytes(StandardCharsets.UTF_8),
                                outMessage.length(),
                                get.getSocketAddress()
                        );
                        try {
                            socket.send(post);
                        } catch (IOException e) {
                            System.err.println("Error occurred while sending message " + e.getMessage());
                        }
                    });
                }
            });
        }
    }

    @Override
    public void close() {
        isRunning = false;
        socket.close();
        pool.shutdownNow();
        try {
            pool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Waiting for threads termination was interrupted");
        }
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("args[] cannot be null");
            return;
        }
        for (String one : args) {
            if (one == null) {
                System.err.println("None of arguments can be null");
                return;
            }
        }
        if (args.length != 2) {
            System.err.println("Wrong number of arguments. Use: <port> <number of treads>");
            return;
        }
        try {
            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Error while parsing integer argument. " + e.getMessage());
        }
    }
}
