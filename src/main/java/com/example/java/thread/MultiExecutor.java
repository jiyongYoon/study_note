package com.example.java.thread;

import java.util.ArrayList;
import java.util.List;

public class MultiExecutor {

    List<Thread> threadList = new ArrayList<>();

    public MultiExecutor(List<Runnable> tasks) {
        for (Runnable task : tasks) {
            Thread thread = new Thread(task);
            threadList.add(thread);
        }
    }

    public void executeAll() {
        this.threadList.forEach(Thread::start);
    }

    public static void main(String[] args) {
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i;
            tasks.add(() -> System.out.println("hello! I'm " + value));
        }
        MultiExecutor multiExecutor = new MultiExecutor(tasks);
        multiExecutor.executeAll();
    }

}
