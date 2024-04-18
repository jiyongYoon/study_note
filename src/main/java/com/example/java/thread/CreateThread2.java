package com.example.java.thread;

public class CreateThread2 {

    public static void main(String[] args) {
        Thread thread = new NewThread();
        thread.start();
        /**
         * My name is Thread[Thread-0,5,main]
         * My name is Thread-0
         */
    }

    private static class NewThread extends Thread {

        @Override
        public void run() {
            System.out.println("My name is " + Thread.currentThread());
            System.out.println("My name is " + this.getName());
        }
    }
}
