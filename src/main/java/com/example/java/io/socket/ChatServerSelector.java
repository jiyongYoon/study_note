package com.example.java.io.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ChatServerSelector {

  public static void main(String[] args) {
    Selector selector;               // 여러 Channel의 이벤트를 감시하는 이벤트 디멀티플렉서
    ServerSocketChannel serverSocketChannel; // 서버용 소켓 채널 (listen 담당)
    int listenPort = 20000;

    try {
      // 1. Selector 생성
      //  - 여러 채널의 I/O 이벤트를 하나의 스레드에서 감시하기 위한 객체
      //  - 내부적으로 OS의 epoll/kqueue/select 등을 사용
      selector = Selector.open();

      // 2. ServerSocketChannel 생성
      //  - 기존 ServerSocket의 NIO 버전
      serverSocketChannel = ServerSocketChannel.open();

      // 서버 소켓에 포트 바인딩
      serverSocketChannel.bind(new InetSocketAddress(listenPort));

      // ⭐ Non-blocking 모드 설정
      //  - 이 채널에서 accept(), read(), write() 호출 시
      //    → 데이터가 없으면 "대기(block)"하지 않고 즉시 리턴
      //  - Selector와 함께 사용하기 위한 필수 조건
      serverSocketChannel.configureBlocking(false);

      // 3. Selector에 채널 등록
      //  - OP_ACCEPT : "클라이언트 연결 요청이 들어왔는지" 감시
      //  - Selector는 이 이벤트가 발생하면 select()를 깨운다
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
      /**
       * 이 순간 JVM은 내부적으로:
       * Linux → epoll_ctl
       * BSD/Mac → kqueue
       * Windows → select / poll
       * 같은 OS 이벤트 디멀티플렉서에
       * “이 fd(소켓)에 이런 이벤트 오면 알려줘”
       * 라고 등록해 둔다.
       */

      System.out.println("NIO Chat Server start...");

      // 4. 공용 ByteBuffer 생성
      //  - 실제로는 클라이언트별 버퍼를 따로 두는 게 안전
      //  - 여기서는 예제 단순화를 위해 하나만 사용
      ByteBuffer buffer = ByteBuffer.allocate(1024);

      // 5. 이벤트 루프 (단일 스레드)
      while (true) {

        // ⭐ 이벤트 대기
        //  - 등록된 채널 중 "준비된 I/O 이벤트"가 있을 때까지 block
        //  - CPU를 낭비하지 않음 (busy waiting 아님)
        selector.select();
        /**
         * 이벤트 루프 스레드는 여기서 커널의 system call을 호출한 후
         * OS가 이벤트 발생 알림을 줄때까지 대기 (WAITING)
         *
         * 이번트 발생 시점은 OS가 '당장 처리 가능한 I/O 이벤트가 하나 이상 있음'을 인지하고 깨움
         */

        // 준비된 이벤트 목록 가져오기
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();

        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove(); // 처리한 이벤트는 반드시 제거 (중복 처리 방지)

          // 6. 연결 요청 처리
          if (key.isAcceptable()) {
            // OP_ACCEPT 이벤트가 발생한 채널은 ServerSocketChannel
            ServerSocketChannel server = (ServerSocketChannel) key.channel();

            // accept() 호출
            //  - Non-blocking 이므로 연결이 준비된 경우에만 SocketChannel 반환
            SocketChannel clientSocketChannel = server.accept();

            // 클라이언트 소켓도 Non-blocking 설정
            clientSocketChannel.configureBlocking(false);

            // 클라이언트 채널을 Selector에 등록
            // OP_READ : "읽을 데이터가 도착했는지" 감시
            clientSocketChannel.register(selector, SelectionKey.OP_READ);

            System.out.println("New client connected...");
            System.out.println("[" + clientSocketChannel.getRemoteAddress() + "]");
          }

          // 7. 데이터 수신 처리
          // => 실제 서버에서는 얼마나 걸릴지 모르기 때문에 이벤트 루프 스레드가 아니라 처리 스레드풀이 따로 있어야 함
          else if (key.isReadable()) {
            SocketChannel clientSocketChannel = (SocketChannel) key.channel();

            buffer.clear(); // 이전 읽기 데이터 제거
            int bytesRead;

            try {
              // Non-blocking read
              //  - 읽을 데이터가 준비된 상태에서만 호출됨 (Selector가 보장)
              bytesRead = clientSocketChannel.read(buffer);
            } catch (IOException e) {
              // 클라이언트 비정상 종료
              System.out.println("Client terminated: " + clientSocketChannel);
              key.cancel();              // Selector에서 제거
              clientSocketChannel.close();
              continue;
            }

            // 정상적인 연결 종료 (EOF)
            if (bytesRead == -1) {
              System.out.println("Client disconnected: " + clientSocketChannel);
              key.cancel();
              clientSocketChannel.close();
              continue;
            }

            // 읽기 모드 → 쓰기 모드 전환
            buffer.flip();

            // ByteBuffer → 문자열 변환
            String msg = new String(buffer.array(), 0, buffer.limit()).trim();

            // 종료 명령 처리
            if (msg.equalsIgnoreCase("exit")) {
              System.out.println("Client exit command received: " + clientSocketChannel);
              key.cancel();
              clientSocketChannel.close();
              continue;
            }

            System.out.println(msg);

            // 모든 클라이언트에게 메시지 브로드캐스트
            sendMessageAll(selector, clientSocketChannel, msg);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // 모든 클라이언트에게 메시지 전송
  private static void sendMessageAll(Selector selector, SocketChannel sender, String msg) throws IOException {

    // 전송할 메시지를 ByteBuffer로 래핑
    ByteBuffer msgBuffer = ByteBuffer.wrap((msg + "\n").getBytes());

    // Selector에 등록된 모든 채널 순회
    for (SelectionKey key : selector.keys()) {
      Channel channel = key.channel();

      // 서버 채널(ServerSocketChannel)은 제외
      if (channel instanceof SocketChannel) {
        SocketChannel target = (SocketChannel) channel;

        // Non-blocking write
        // ⚠ 실제 서비스에서는 write가 한 번에 다 안 될 수 있음
        // → remaining() 체크 및 OP_WRITE 등록 필요
        target.write(msgBuffer);

        // 다음 채널 전송을 위해 position 초기화
        msgBuffer.rewind();
      }
    }
  }
}

