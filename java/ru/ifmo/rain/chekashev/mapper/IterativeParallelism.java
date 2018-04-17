package ru.ifmo.rain.chekashev.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import ru.ifmo.rain.chekashev.arrayset.ArraySet;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

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

        List<Thread> threads = new ArrayList<>();
        final ArrayList<List<U>> bucket = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            bucket.add(new ArrayList<U>());
            final int l = (int) (((long) i) * values.size() / n);
            final int r = (int) (((long) i + 1) * values.size() / n);
            final int where = i;
            threads.add(new Thread(() -> {
                Stream<T> local = values.subList(l, r).stream();
                if (predicate != null) {
                    local = local.filter(predicate);
                }
                if (comparator != null) {
                    Optional<T> opt = local.max(comparator);
                    bucket.get(where).add(mapper.apply(opt.get()));
                } else {
                    List<U> mapped = local.map(mapper).collect(Collectors.toList());
                    synchronized (bucket) {
                        bucket.set(where, mapped);
                    }
                }
            }));
        }
        startAll(threads);
        List<U> ret = new ArrayList<U>();
        for (List<U> one : bucket) {
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
