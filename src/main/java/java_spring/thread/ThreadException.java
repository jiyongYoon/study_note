package java_spring.thread;

public class ThreadException {

    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("Hello Exception!!");
            }
        });
        thread.setName("HelloThread");
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println(
                    "스레드 내에서 Exception이 발생하였습니다. t.getName()=" + t.getName() + ", e.getMessage="
                        + e.getMessage());
            }
        });
        thread.start();

        /**
         * 스레드 내에서 Exception이 발생하였습니다. t.getName()=HelloThread, e.getMessage=Hello Exception!!
         */
    }
}
