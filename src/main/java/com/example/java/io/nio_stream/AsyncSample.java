package com.example.java.io.nio_stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

public class AsyncSample {

  private static final String filePath = System.getProperty("user.dir") + "\\tmp\\test.txt";

  public static void main(String[] args) throws IOException, InterruptedException {
    AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(Paths.get(filePath),
        StandardOpenOption.WRITE, // 쓰기 작업 허용
        StandardOpenOption.CREATE); // 없으면 파일 새로 생성
    // 여기까지는 파일을 열기만 함

    String data = "AsynchronousFileChannel I/O sample";
    ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
    CountDownLatch latch = new CountDownLatch(1);

    // 비동기로 즉시! 반환됨. 즉, 블로킹 없이 넘어감
    asyncChannel.write(
        buffer, // OS가 읽어갈 데이터가 담긴 ByteBuffer
        0, // 파일 내 쓰기 시작 위치 (offset, 랜덤 접근 가능!)
        "Async write: ", // 콜백으로 다시 전달될 attachment (문맥 정보를 담을때 사용)
        new CompletionHandler<Integer, String>() {
      // I/O 성공 시 호출되는 콜백
      @Override
      public void completed(Integer result, String attachment) {
        System.out.println(attachment + result + " bytes");
        latch.countDown();
      }

      // I/O 실패 시 호출되는 콜백
      @Override
      public void failed(Throwable exc, String attachment) {
        System.out.println("ERROR: " + exc.getMessage());
        latch.countDown();
      }
    });

    System.out.println("Waiting...");
    latch.await(); // I/O 콜백 올때까지 대기
    System.out.println("End of main thread.");

    asyncChannel.close();
  }

  /**
   * Waiting...
   * Async write: 34 bytes
   * End of main thread.
   */

}
