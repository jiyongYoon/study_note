# 1. 기존 Stream 기반 File I/O의 한계

1. 파일을 순차 스트림 모델로 다루기 때문에 임의 위치 접근, 병렬 처리, 재사용에 불리하다.
2. Blocking I/O 모델로 인해 스레드가 I/O 동안 점유되어 컨텍스트 스위칭 비용이 증가하고 대규모 병렬 처리에 불리하다.
3. Page Cache에 있더라도 항상 User buffer(JVM Heap)로 복사되므로 불필요한 메모리 복사와 GC 부담이 발생한다.

# 2. NIO의 접근 개념

1. 파일을 addressable한 바이트 시퀀스로 바라보고 position 기반의 랜덤 엑세스가 가능하다.
2. 스레드 block을 최소화하여 OS의 I/O 처리 모델을 효율적으로 활용한다.
3. Page Cache를 중심으로 동작하여 copy 비용을 줄이고 병렬 접근에 유리하다.

# 3. NIO ver.1 -> NIO ver.2

## 1) NIO ver.1
1. Java 1.4
2. Multiplexing I/O
3. Single thread
4. `SocketChannel` & `ServerSocketChannel`

## 2) NIO ver.2 - AIO(Asynchronous I/O)
1. Java 7 
2. CompletionHandler의 Callback 구조
   - File I/O 요청 시 callback 메서드를 같이 넘기면 OS가 I/O 이후 JVM이 callback 메서드를 호출해준다.
3. OS 수준에서 통제되는 Thread pool
4. `AsyncronousSocketChannel` & `AsyncronousServerSocketChannel`