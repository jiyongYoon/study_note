# 1. 기존 Stream 기반 File I/O의 한계

1. 파일을 순차 스트림 모델로 다루기 때문에 임의 위치 접근, 병렬 처리, 재사용에 불리하다.
2. Blocking I/O 모델로 인해 스레드가 I/O 동안 점유되어 컨텍스트 스위칭 비용이 증가하고 대규모 병렬 처리에 불리하다.
3. Page Cache에 있더라도 항상 User buffer(JVM Heap)로 복사되므로 불필요한 메모리 복사와 GC 부담이 발생한다.

# 2. NIO의 접근 개념

1. 파일을 addressable한 바이트 시퀀스로 바라보고 position(offset) 기반의 랜덤 엑세스가 가능하다.
2. 스레드 block을 최소화하여 OS의 I/O 처리 모델을 효율적으로 활용한다.
3. Page Cache를 중심으로 동작하여 copy 비용을 줄이고 병렬 접근에 유리하다.

## MMF(Memory Mapped File)
- File을 메모리에 매핑하여 사용하는 개념
- 파일을 '읽는' 것이 아니라 '프로세스의 가상 메모리 공간에 연결'하는 것
- java에서 사용
  ```java
  MappedByteBuffer buffer = channel.map(...);
  ```
- c에서 사용
  ```c
  mmap(fd, offset, size)
  ```

### (1) MMF 구조
```text
[JVM Heap]               ← 거의 안 씀
[JVM Virtual Address]    ← mmap 된 주소
[Page Cache]             ← 실제 데이터
[Disk]
```

### (2) 읽기 흐름  - `필요한 페이지만 지연 로드`
```java
MappedByteBuffer buf = channel.map(...)
buf.get(i)
```

1. CPU가 해당 주소에 접근
2. page fault (cache miss)
3. OS가 디스크에서 데이터 읽음 (필요한 페이지만 로드)
4. Page Cache 적재
5. 다시 실행 -> 데이터 반환

### (3) 쓰기 흐름 - `쓰기 지연`
```java
buf.put(...)
```

1. Page Cache에 기록
2. 디스크 반영은 OS가 나중에 (fsync, 커널 정책 등)

### (4) MMF의 위험성
1. 메모리 관리권이 없다. 즉, JVM이 관리하지 않고 OS가 관리하기 때문에 메모리 압박에 대한 예측이 어렵다.
2. java에서는 unmap 제어가 불가능하다. `MappedByteBuffer buf = channel.map(...)` 메모리의 해제 후 GC는 진행되지만, 이후 Page Cache에서 내리는 것은 제어가 불가능하다. 온전히 OS의 몫이 된다.

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