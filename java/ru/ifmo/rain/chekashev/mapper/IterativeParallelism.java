package ru.ifmo.rain.chekashev.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.rain.chekashev.arrayset.ArraySet;

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

    private void startAll(List<Thread> threads) throws InterruptedException {
        for (Thread one : threads) {
            one.start();
        }
        for (Thread one : threads) {
            one.join();
        }
    }

    private <T, U> List<U> universal(int n,
                                     final List<T> values,
                                     final Predicate<? super T> predicate,
                                     final Comparator<? super T> comparator,
                                     final Function<? super T, ? extends U> mapper) throws InterruptedException {
        if (n > values.size()) {
            n = values.size();
        }

        final Function<List<T>, List<U>> task = new Function<List<T>, List<U>>() {
            @Override
            public List<U> apply(List<T> one) {
                Stream<T> local = one.stream();
                if (predicate != null) {
                    local = local.filter(predicate);
                }
                if (comparator != null) {
                    Optional<T> opt = local.max(comparator);
                    List<U> single = new ArrayList<>();
                    single.add(mapper.apply(opt.get()));
                    return single;
                } else {
                    return local.map(mapper).collect(Collectors.toList());
                }
            }
        };

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
            startAll(threads);
        }

        List<U> ret = new ArrayList<>();
        for (List<U> one : stash) {
            ret.addAll(one);
        }
        return ret;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        List<String> stringList = universal(threads, values, null, null, Object::toString);
        return String.join("", stringList);
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return universal(threads, values, predicate, null, a -> a);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return universal(threads, values, null, null, f);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        List<T> raw = universal(threads, values, null, comparator, a -> a);
        return raw.stream().max(comparator).get();
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
