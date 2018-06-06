package ru.ifmo.rain.chekashev.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {
    private ParallelMapper manager = null;

    public IterativeParallelism() {

    }

    public IterativeParallelism(ParallelMapper one) {
        manager = one;
    }

    private <T, U> List<U> splitRun(int n,
                                     final List<T> values,
                                     final Function<List<T>, List<U>> task) throws InterruptedException {
        if (n > values.size()) {
            n = values.size();
        }

        List<List<T>> partition = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int l = (int) (((long) i) * values.size() / n);
            int r = (int) (((long) i + 1) * values.size() / n);
            partition.add(values.subList(l, r));
        }

        List<List<U>> stash = new ArrayList<>();
        if (manager != null) {
            stash = manager.map(task, partition);
        } else {
            List<Thread> threads = new ArrayList<>();
            final List<List<U>> finalStash = stash;
            for (int i = 0; i < n; i++) {
                final int where = i;
                stash.add(new ArrayList<>());
                threads.add(new Thread(() -> {
                    finalStash.set(where, task.apply(partition.get(where)));
                }));
            }
            threads.forEach(Thread::start);
            List<InterruptedException> badJoins = new ArrayList<>();
            for (Thread one : threads) {
                try {
                    one.join();
                } catch (InterruptedException e) {
                    badJoins.add(e);
                }
            }
            if (badJoins.size() > 0) {
                System.err.println(badJoins.size() + " threads haven't joined");
                throw new MultiJoinErrorException("Some threads haven't joined", badJoins);
            }
        }

        List<U> ret = new ArrayList<>();
        for (List<U> one : stash) {
            ret.addAll(one);
        }
        return ret;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return String.join("", map(threads, values, Object::toString));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return splitRun(threads, values,
                one -> one.stream().filter(predicate).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return splitRun(threads, values,
                one -> one.stream().map(f).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return splitRun(threads, values,
                one -> one.stream().max(comparator).stream().collect(Collectors.toList()))
                .stream().max(comparator).get();
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return filter(threads, values, predicate).size() == values.size();
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
