package ru.ifmo.rain.chekashev.mapper;

import java.util.List;

public class MultiJoinErrorException extends InterruptedException {
    private List<InterruptedException> list;

    public MultiJoinErrorException(String message, List<InterruptedException> list) {
        super(message);
        this.list = list;
    }

    public List<InterruptedException> getList() {
        return list;
    }
}
