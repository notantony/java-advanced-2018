package ru.ifmo.rain.chekashev.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> queue;
    private final List<Thread> pool;

    public ParallelMapperImpl(int threads) {
        pool = new ArrayList<>();
        queue = new ArrayDeque<>();
        for (int i = 0; i < threads; i++) {
            pool.add(new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Runnable one;
                        synchronized (queue) {
                            while (queue.isEmpty()) {
                                queue.wait();
                            }
                            one = queue.poll();
                        }
                        one.run();
                    }
                } catch (InterruptedException e) {

                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
            pool.get(i).start();
        }
    }

    private class Counter {
        int x = 0;

        public void inc() {
            x++;
        }

        public int get() {
            return x;
        }
    }

    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args) throws InterruptedException {//TODO: check sync
        final List<R> bucket = new ArrayList<>();
        final Counter done = new Counter();
        for (int i = 0; i < args.size(); i++) {
            bucket.add(null);
            final int where = i;
            synchronized (queue) {
                queue.add(new Runnable() {
                    @Override
                    public void run() {
                        R local = f.apply(args.get(where));
                        bucket.set(where, local);
                        synchronized (done) {
                            done.inc();
                            if (done.get() == args.size()) {
                                done.notify();
                            }
                        }
                    }
                });
                queue.notify();
            }
        }
        synchronized (done) {
            if (done.get() != args.size()) {
                done.wait();
            }
        }
        return bucket;
    }

    @Override
    public void close() {
        int badJoins = 0;
        for (Thread one : pool) {
            one.interrupt();
            try {
                one.join();
            } catch (InterruptedException e) {
                badJoins++;
            }
        }
        if(badJoins > 0){
            System.err.println(badJoins + " thread haven't joined");
        }
    }
}
