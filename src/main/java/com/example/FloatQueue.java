package com.example;

import java.util.NoSuchElementException;

public class FloatQueue {
    private float[] data;
    private int manyItems;
    private int front;
    private int rear;

    public FloatQueue() {
        final int INITIAL_CAPACITY = 16;
        manyItems = 0;
        data = new float[INITIAL_CAPACITY];
    }

    public FloatQueue(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("initialCapacity is negative: " + initialCapacity);
        manyItems = 0;
        data = new float[initialCapacity];
    }

    public void ensureCapacity(int minimumCapacity) {
        float biggerArray[];
        int n1, n2;

        if (data.length >= minimumCapacity)
            return;
        else if (manyItems == 0)
            data = new float[minimumCapacity];
        else if (front <= rear) {
            biggerArray = new float[minimumCapacity];
            System.arraycopy(data, front, biggerArray, front, manyItems);
            data = biggerArray;
        } else {
            biggerArray = new float[minimumCapacity];
            n1 = data.length - front;
            n2 = rear + 1;
            System.arraycopy(data, front, biggerArray, 0, n1);
            System.arraycopy(data, 0, biggerArray, n1, n2);
            front = 0;
            rear = manyItems - 1;
            data = biggerArray;
        }
    }

    public int getCapacity() {
        return data.length;
    }

    public float getFront() {
        float answer;

        if (manyItems == 0)
            throw new NoSuchElementException("Queue underflow");
        answer = data[front];
        front = nextIndex(front);
        manyItems--;
        return answer;
    }

    public void insert(float item) {
        if (manyItems == data.length) {
            ensureCapacity(manyItems * 2 + 1);
        }

        if (manyItems == 0) {
            front = 0;
            rear = 0;
        } else
            rear = nextIndex(rear);

        data[rear] = item;
        manyItems++;
    }

    private int nextIndex(int i) {
        if (++i == data.length)
            return 0;
        else
            return i;
    }

    public int size() {
        return manyItems;
    }
}