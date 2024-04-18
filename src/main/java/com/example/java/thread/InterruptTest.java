package com.example.java.thread;

import java.math.BigInteger;

public class InterruptTest {

    public static void main(String[] args) {
        Thread thread = new Thread(new BlockingTask());

        thread.start(); // 스레드가 실행되어 sleep 상태였는데
        thread.interrupt(); // interrupt 되어 Exception 발생 및 처리
        /**
         * block!
         * exiting sleep thread
         */


        Thread thread1 = new Thread(new LongComputationTask(
            new BigInteger("200000"), new BigInteger("1000000000")
        ));

        thread1.start(); // 스레드가 실행되어 큰 계산 중이였는데
        thread1.interrupt(); // interrupt 되어 작업 마무리
        /**
         * you are interrupted!! exit now!
         * 200000^1000000000 = 0
         */

        Thread thread2 = new Thread(new NoInterruptLongComputationTask(
            new BigInteger("200000"), new BigInteger("10000000")
        ));

        thread2.setDaemon(true); // 해당 스레드를 Daemon으로 돌리면 main스레드에서 더이상 관여하지 않는다.
        thread2.start();
        thread2.interrupt(); // 따라서 interrupt 여부와 상관없이 main thread가 종료되면 앱이 종료된다.

        System.out.println("main thread complete");
        /**
         * block!
         * exiting sleep thread
         * you are interrupted!! exit now!
         * main thread complete
         * 200000^1000000000 = 0
         */
    }

    private static class BlockingTask implements Runnable {

        @Override
        public void run() {
            System.out.println("block!");
            try {
                Thread.sleep(200000); // interrupt가 발생하면 InterruptedException 발생
            } catch (InterruptedException e) {
                System.out.println("exiting sleep thread");
            }
        }
    }

    private static class LongComputationTask implements Runnable {
        private final BigInteger base;
        private final BigInteger power;

        public LongComputationTask(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            System.out.println(base + "^" + power + " = " + pow(base, power));
        }
        
        private BigInteger pow(BigInteger base, BigInteger power) {
            BigInteger result = BigInteger.ONE;

            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                result = result.multiply(base);
                // 이 경우에는 thread.sleep 처럼 InterruptedException이 발생하지 않기 때문에
                // 명시적으로 긴 작업중에 interrupt를 받을 수 있도록 처리해주어야 함
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("you are interrupted!! exit now!");
                    return BigInteger.ZERO;
                }
            }

            return result;
        }
    }

    private static class NoInterruptLongComputationTask implements Runnable {
        private final BigInteger base;
        private final BigInteger power;

        public NoInterruptLongComputationTask(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            System.out.println(base + "^" + power + " = " + pow(base, power));
        }

        private BigInteger pow(BigInteger base, BigInteger power) {
            BigInteger result = BigInteger.ONE;

            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                result = result.multiply(base);
            }

            return result;
        }
    }

}
