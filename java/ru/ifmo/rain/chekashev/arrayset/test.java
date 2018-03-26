package ru.ifmo.rain.chekashev.arrayset;

import java.util.*;

class qwe implements Comparator<Integer> {
    public int compare(Integer a, Integer b) {
        //System.out.println("GUSI");
        if( a> b) return 1;
        else if(a < b) return -1;
        else return 0;
    }
}

public class test {
    public static void main(String args[]) {
        ArrayList<Integer> b = new ArrayList<Integer>(3);
        b.add(0);
        b.add(1);
        b.add(3);
        b.add(4);
        b.add(5);
        b.add(6);
        b.add(7);
        b.add(9);
        ArraySet<Integer> a = new ArraySet<Integer>(b, new qwe());
        a = a.descendingSet();
        System.out.println(a);
        System.out.println(a = a.subSet(5, false,  0, true));
        a = a.descendingSet();
        System.out.println(a);
        System.out.println(a.subSet(2, 5));
    }
}
