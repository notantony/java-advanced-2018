package ru.ifmo.rain.chekashev.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final Integer perHost;
    private final ExecutorService downloadService, extractService;
    private final Set<String> downloaded = ConcurrentHashMap.newKeySet();
    //private final Set<String> extracted = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> atm = new ConcurrentHashMap<>();
    private final List<String> stash = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, IOException> errors = new ConcurrentHashMap<>();
    //private final Counter todo = new Counter();


    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        downloadService = Executors.newFixedThreadPool(downloaders);
        extractService = Executors.newFixedThreadPool(extractors);
    }


    private class Counter {
        int x = 0, id = new Random().nextInt();

        public synchronized void inc() {
            x++;
        }

        public synchronized void dec() {
            x--;
            if(x == 0) {
                this.notify();
            }
        }

        public synchronized int get() {
            return x;
        }

        public synchronized int getID() {
            return id;
        }
    }

    private Counter extNumber = new Counter();

    private void downloadOne(String url, int depth, final Counter todo) {
        downloaded.add(url);
        todo.inc();
        downloadService.submit(() -> {
            System.err.println("Download started with URL " + url);
            Document document;
            try {
                document = downloader.download(url);
            } catch (IOException e) {
                errors.put(url, e);
                System.err.println("Download ended unsuccessfully with URL " + url);
                todo.dec();
                System.err.println(todo.getID() + "_" + todo.get());
                //Thread.currentThread().interrupt();
                return;
            }
            stash.add(url);
            if (depth != 0) {
                //extracted.add(url);
                todo.inc();
                System.err.println("Submit extraction " + extNumber.get());
                extractService.submit(() -> {
                    System.err.println("Extraction started with URL " + url);
                    List<String> links;
                    try {
                        links = document.extractLinks();
                    } catch (IOException e) {
                        errors.put(url, e);
                        System.err.println("Extraction ended unsuccessfully with URL " + url);
                        todo.dec();
                        System.err.println(todo.getID() + "_" + todo.get());
                        //Thread.currentThread().interrupt();
                        return;
                    }
                    /*for (String one : links) {
                        System.err.println(one);
                    }
                    for (String one : downloaded) {
                        System.err.println(one);
                        System.err.println(downloaded.contains(one));
                    }*/
                    for (String one : links) {
                        if (!downloaded.contains(one)) {
                            System.err.println("adding " + one);
                            downloadOne(one, depth - 1, todo);
                        }
                    }
                    System.err.println("Extraction ended with URL " + url);
                    todo.dec();
                    System.err.println(todo.getID() + "_" + todo.get());
                    //Thread.currentThread().interrupt();
                });
            }
            System.err.println("Download ended with URL " + url);
            todo.dec();
            System.err.println(todo.getID() + "_" + todo.get());
            //Thread.currentThread().interrupt();
        });
    }

    @Override//TODO: download with same downloader after a week must download same file again
    public Result download(String url, int depth) {
        final Counter todo = new Counter();
        downloadOne(url, depth, todo);
        synchronized (todo) {
            try {
                while (todo.get() != 0) {
                    todo.wait();
                }
            } catch (InterruptedException e) {
                System.err.println("Downloading was terminated, result is undefined");
            }
        }
        return new Result(stash, errors);
//        try {
//            while (!downloadService.isShutdown()) {
//                System.err.println(downloadService.isShutdown());
//                System.err.println(downloadService.isTerminated());
//            }
//            downloadService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//            extractService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//        }
    }

    @Override
    public void close() {

    }
/*
    public static void main(String[] args) {
        //WebCrawler url [downloads [extractors [perHost]]]
        //CachingDownloader
    }*/
}
