package ru.ifmo.rain.chekashev.arrayset;

import java.util.*;

class DirectionList<G extends Comparable<? super G>> extends AbstractList<G> implements RandomAccess {
    final private boolean reversed;
    final private List<G> a;

    DirectionList(DirectionList<G> one) { //reverse constructor
        this.a = one.a;
        this.reversed = !one.reversed;
    }

    DirectionList(List<G> sorted) { //main constructor
        this.a = sorted;
        this.reversed = false;
    }

    @Override
    public G get(int i) {
        return a.get(reversed ? size() - i - 1 : i);
    }

    @Override
    public int size() {
        return a.size();
    }
}