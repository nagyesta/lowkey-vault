package com.github.nagyesta.lowkeyvault.steps;

import java.util.List;
import java.util.function.Consumer;

public class ListContainer<T> {
    private final List<T> list;

    public ListContainer(final List<T> list) {
        this.list = list;
    }

    public int size() {
        return this.list.size();
    }

    public void forEach(final Consumer<T> consumer) {
        this.list.forEach(consumer);
    }

    public List<T> getList() {
        return list;
    }
}
