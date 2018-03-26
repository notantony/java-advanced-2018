package ru.ifmo.rain.chekashev.arrayset;


import java.util.*;
import java.util.Collections;

public class ArraySet<E extends Comparable<? super E>> extends AbstractSet<E> implements NavigableSet<E> {
    private final DirectionList<E> a;
    private final Comparator<? super E> cmp;

    private E inRange(int i) {
        if (i >= 0 && i < a.size()) {
            return a.get(i);
        }
        return null;
    }

    private int lBorder(E one, boolean inclusive) {
        int i = Collections.binarySearch(a, one, cmp);
        if (i >= 0) {
            return i - (inclusive ? 0 : 1);
        }
        return (-i - 1) - 1;
    }

    private int rBorder(E one, boolean inclusive) {
        int i = Collections.binarySearch(a, one, cmp);
        if (i >= 0) {
            return i + (inclusive ? 0 : 1);
        }
        return (-i - 1);
    }

    private ArraySet(DirectionList<E> one, Comparator<? super E> cmp) {
        a = one;
        this.cmp = cmp;
    }

    public ArraySet(Collection<E> one, Comparator<? super E> cmp) {
        TreeSet<E> tmp = new TreeSet<E>(cmp);
        tmp.addAll(one);
        a = new DirectionList<E>(new ArrayList<E>(tmp));
        this.cmp = cmp;
    }

    public ArraySet(Collection<E> one) {
        this(one, null);
    }

    public ArraySet() {
        this(Collections.emptyList());
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(a, (E) o, cmp) >= 0;
    }

    public E lower(E e) {
        return inRange(lBorder(e, false));
    }

    public E floor(E e) {
        return inRange(lBorder(e, true));
    }

    public E higher(E e) {
        return inRange(rBorder(e, false));
    }

    public E ceiling(E e) {
        return inRange(rBorder(e, true));
    }

    public E pollFirst() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    public E pollLast() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    public Iterator<E> iterator() {
        return Collections.unmodifiableList(a).iterator();
    }

    public ArraySet<E> descendingSet() {
        return new ArraySet<E>(new DirectionList<E>(a), Collections.reverseOrder(cmp));
    }

    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    public ArraySet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int l = rBorder(fromElement, fromInclusive);
        int r = lBorder(toElement, toInclusive) + 1;
        if (l > r) {
            r = l;
        }
        return new ArraySet<E>(new DirectionList<E>(a.subList(l, r)), cmp);
    }


    public ArraySet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty()) return this;
        return subSet(first(), true, toElement, inclusive);
    }

    public ArraySet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty()) return this;
        return subSet(fromElement, inclusive, last(), true);
    }

    public Comparator<? super E> comparator() {
        return cmp;
    }

    public ArraySet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    public ArraySet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    public ArraySet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    public E first() throws NoSuchElementException {
        if (a.isEmpty()) throw new NoSuchElementException("Cannot get first element in empty set");
        return a.get(0);
    }

    public E last() {
        if (a.isEmpty()) throw new NoSuchElementException("Cannot get last element in empty set");
        return a.get(a.size() - 1);
    }

    public int size() {
        return a.size();
    }
}
