package com.example.java.thread;

public class CreateThread {

    public static void main(String[] args) throws InterruptedException {
        // 스레드 객체 자체는 기본적으로 비어있기 때문에, Runnable 인터페이스를 구현해 생성자에 전달한다.
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                // 스레드가 실행되지마자 실행될 코드
                System.out.println("hello!, my name is " + Thread.currentThread().getName());
            }
        });

        thread1.setName("HelloThread!");

        System.out.println(
            "Thread.currentThread().getName() = " + Thread.currentThread().getName());
        thread1.start();
        System.out.println(
            "Thread.currentThread().getName() = " + Thread.currentThread().getName());

        Thread.sleep(5000); // 이 스레드를 해당 시간동안 cpu 스케쥴링에 할당하지 말라는 뜻
        // 이 이후에는 스케쥴링의 알고리즘에 따라 다시 배치가 되게 된다. (이 시간 이후 바로 실행된다는 뜻이 아님)

        // java8 이후로는 람다 사용 가능
        Thread thread2 = new Thread(() -> System.out.println(
            "hello2!, my priority is " + Thread.currentThread().getPriority()));
        thread2.setPriority(Thread.MAX_PRIORITY);
        thread2.start();

        /**
         * Thread.currentThread().getName() = main
         * Thread.currentThread().getName() = main
         * hello!, my name is HelloThread!
         * hello2!, my priority is 10
         */
    }
}
