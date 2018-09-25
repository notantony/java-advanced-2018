package ru.ifmo.rain.chekashev.hello;


import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        final SocketAddress address;
        try {
            address = new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (UnknownHostException e) {
            System.err.println("Bad host " + e.getMessage());
            return;
        }
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int threadId = i;

            pool.submit(new Thread(() -> {
                try (DatagramSocket socket = new DatagramSocket(address)) {
                    byte buffer[] = new byte[socket.getReceiveBufferSize()];
                    for (int requestId = 0; requestId < requests; requestId++) {
                        String outMessage = (prefix + threadId + "_" + requestId);
                        while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                            DatagramPacket post = new DatagramPacket(
                                    outMessage.getBytes(StandardCharsets.UTF_8),
                                    outMessage.length(),
                                    address
                            );
                            try {
                                socket.send(post);
                            } catch (IOException e) {
                                System.err.println("Error occurred while sending message " + e.getMessage());
                                break;
                            }

                            DatagramPacket get = new DatagramPacket(buffer, socket.getReceiveBufferSize());
                            try {
                                socket.receive(get);
                            } catch (IOException e) {
                                System.err.println("Error occurred while receiving message " + e.getMessage());
                                break;
                            }

                            String inMessage = new String(get.getData(), get.getOffset(), get.getLength(), StandardCharsets.UTF_8);
                            if (inMessage.contains(inMessage)) {
                                System.out.println(inMessage);
                                break;
                            } else {
                                System.out.println("Unexpected message received. Retrying request.");
                            }
                        }
                    }
                } catch (SocketException e) {
                    System.err.println("Socket error on thread " + threadId + ". " + e.getMessage());
                }
            }));
        }
        pool.shutdown();
        try {
            pool.awaitTermination(((long)threads) * requests * 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Waiting for threads termination was interrupted");
        }
    }

    public static void main(final String[] args) {
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
        if (args.length != 5) {
            System.err.println("Wrong number of arguments. Use: <host> <port> <message> <number of treads> <requests per thread>");
            return;
        }
        int port, threads, requests;
        try {
            port = Integer.parseInt(args[1]);
            threads = Integer.parseInt(args[3]);
            requests = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Error while parsing integer argument. " + e.getMessage());
            return;
        }
        if (port < 0) {
            System.err.println("Port cannot be negative");
            return;
        }
        if (threads <= 0 || requests <= 0) {
            System.err.println("Number of threads/request should be positive");
            return;
        }
        new HelloUDPClient().run(args[0], port, args[2], threads, requests);
    }
}
