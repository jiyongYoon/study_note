package com.example.java.io.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * back-pressure, write 완료 콜백 등이 더 추가되어야 함
 */
public class ChatServerAio {

  private static final Set<AsynchronousSocketChannel> clients = ConcurrentHashMap.newKeySet(); // 여러 스레드에서 호출될 수 있기 때문에 thread-safe

  public static void main(String[] args) throws IOException {
    int port = 20000;

    ExecutorService es = Executors.newFixedThreadPool(8);
    AsynchronousChannelGroup asynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(es);
    AsynchronousServerSocketChannel server =
        AsynchronousServerSocketChannel.open(asynchronousChannelGroup).bind(new InetSocketAddress(port));

    System.out.println("AIO Chat Server start...");

    // 즉시 반환 => AIO의 accept는 non-blocking이며,
    // 실제 연결 대기는 OS 또는 내부 thread pool이 담당하고
    // 완료 시 CompletionHandler가 호출된다.
    //// CompletionHandler는 AsynchronousChannelGroup에 속한 내부 스레드 풀의 스레드에서 실행된다. (main thread 아님!)
    //// AsynchronousChannelGroup 스레드풀: JVM이 OS의 I/O와 매커니즘과 강하게 결합하여 만든 스레드풀 - `AsynchronousServerSocketChannel.open()` 여기서 필요에 따라 스레드풀이 생성됨
    //// I/O 완료 이벤트를 받아서 CompletionHandler 콜백을 실행하는 역할을 함
    server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
      @Override
      public void completed(AsynchronousSocketChannel client, Void attachment) {
        try {
          System.out.println("New client connected: " + client.getRemoteAddress());
        } catch (IOException e) {
          System.out.println("Failed to get client address");
        }

        server.accept(null, this); // 재등록 - AIO의 accept는 1회성이기 때문에 이 과정을 계속 반복하게 하려고 -> 즉 비동기 체인 만들기
        clients.add(client); // 앱에서 관리대상으로 등록

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        readMessage(client, buffer);
      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        System.out.println("Failed to accept: " + exc.getMessage());
      }
    });
    /**
     * 이렇게 AIO - callback 구조로 OS의 지원을 받으면
     * 1. 시스템 수준에서 스레드들을 생성하고 Pool에서 관리하기 때문에 응답성이 높음
     * 2. Memory Copy가 최적화됨 (NIC에서 OS 레벨의 SocketBuffer로 가지 않고 바로 AppBuffer 메모리 위치로 copy)
     */

    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      System.out.println("Server interrupted.");
    }
  }

  private static void readMessage(AsynchronousSocketChannel client, ByteBuffer buffer) {
    // 이 역시 non-blocking read 요청 제출
    // 완료 시 CompletionHandler가 호출됨
    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void attachment) {
        if (result == -1) {
          disconnectClient(client);
          return;
        }

        buffer.flip(); // 버퍼 안에서 데이터가 있는 지점까지 끊음
        String msg = new String(buffer.array(), 0, buffer.limit()).trim(); // 데이터 있는 지점까지 문자 읽기
        buffer.clear(); // 버퍼 정보 초기화

        try {
          System.out.println("Received: " + msg + " from " + client.getRemoteAddress());
        } catch (IOException e) {
          System.out.println("Unknown client message.");
        }

        if (msg.equalsIgnoreCase("exit")) {
          disconnectClient(client);
          return;
        }

        sendMessageAll(client, msg); // 메시지 전송
        readMessage(client, buffer); // 재등록
      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        System.out.println("Read failed: " + exc.getMessage());
        disconnectClient(client);
      }
    });
  }

  private static void sendMessageAll(AsynchronousSocketChannel sender, String msg) {
    for (AsynchronousSocketChannel client : clients) {
      if (!client.equals(sender) && client.isOpen()) {
        ByteBuffer buffer = ByteBuffer.wrap((msg + "\n").getBytes());
        client.write(buffer); // 여기서도 CompletionHandler 콜백 세팅도 가능
      }
    }
  }

  private static void disconnectClient(AsynchronousSocketChannel client) {
    try {
      System.out.println("Client disconnected: " + client.getRemoteAddress());
    } catch (IOException e) {
      System.out.println("Unknown client disconnected.");
    }
    clients.remove(client);
    try {
      client.close();
    } catch (IOException e) {
      System.out.println("Error closing client channel.");
    }
  }

}
