package com.a51integrated.sfs2x.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class RingBuffer<T> implements Iterable<T> {
    private final Object[] data;
    private int head = 0;
    private int size = 0;

    public RingBuffer(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity must be > 0");

        this.data = new Object[capacity];
    }

    public int capacity() {
        return data.length;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == data.length;
    }

    /**
     * Adds an element. If buffer is full, overwrites the oldest element.
     */
    public void add(T value) {
        if (value == null) throw new NullPointerException("null values are not supported");

        int tail = (head + size) % data.length;

        if (isFull()) {
            // overwrite oldest at head, then move head forward
            data[head] = value;
            head = (head + 1) % data.length;
        } else {
            data[tail] = value;
            size++;
        }
    }

    /**
     * Returns a slot to write into, allocating via supplier if empty.
     * If buffer is full, the oldest slot is reused.
     */
    public T acquireSlot(java.util.function.Supplier<T> supplier) {
        if (supplier == null) throw new NullPointerException("supplier must not be null");

        int tail = (head + size) % data.length;
        int index;

        if (isFull()) {
            index = head;
            head = (head + 1) % data.length;
        } else {
            index = tail;
            size++;
        }

        @SuppressWarnings("unchecked")
        T v = (T) data[index];
        if (v == null) {
            v = supplier.get();
            data[index] = v;
        }
        return v;
    }

    /**
     * Returns oldest element without removing, or null if empty.
     */
    public T peek() {
        if (isEmpty()) return null;
        @SuppressWarnings("unchecked")
        T v = (T) data[head];
        return v;
    }

    /**
     * Removes and returns oldest element, or null if empty.
     */
    public T poll() {
        if (isEmpty()) return null;

        @SuppressWarnings("unchecked")
        T v = (T) data[head];
        data[head] = null; // help GC
        head = (head + 1) % data.length;
        size--;
        return v;
    }

    public T getAt(int index) {
        @SuppressWarnings("unchecked")
        T v = (T) data[index];
        return v;
    }

    public T getOrCreateAt(int index, java.util.function.Supplier<T> supplier) {
        @SuppressWarnings("unchecked")
        T v = (T) data[index];
        if (v == null) {
            v = supplier.get();
            data[index] = v;
        }
        return v;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            data[(head + i) % data.length] = null;
        }
        head = 0;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                int idx = (head + i) % data.length;
                i++;
                @SuppressWarnings("unchecked")
                T v = (T) data[idx];
                return v;
            }
        };
    }
}
