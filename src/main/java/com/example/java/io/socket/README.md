# NIO와 AIO의 차이를 확인하는 관점

> I/O 완료를 누가 책임지고, 누가 깨어나는가?

## NIO (Selector 기반) ─ `Ready-based I/O`

```text
[Java Thread]
    |
    | selector.select() - blocking
    ↓
[OS epoll/kqueue]
    |
    | "읽을 준비 됐음"
    ↓
[Java Thread 깨어남]
    |
    | read()/write() 직접 수행
```

- OS가 "이 fd(file descriptor)는 읽을 준비가 됐어"를 알려줌
- 실제 `read()`, `write()`는 JVM에서 직접 호출
- **즉, I/O 수행 자체는 JVM 스레드 책임**

## AIO (AsynchronousFileChannel / AsynchronousSocketChannel) ─ `Completion-based I/O`

```text
(Windows IOCP 기준)

[Java Thread]
    |
    | async read/write 작업 요청(제출) - non-blocking
    ↓
[OS 커널]
    |
    | 실제 I/O 수행
    |
    | 완료되면
    ↓
[Completion Queue]
    ↓
[JVM Thread Pool → callback]
```

- JVM 스레드는 I/O 요청만 하고 바로 리턴
- **실제 I/O 수행 및 완료까지 OS가 책임**
- 완료되면 OS가 JVM에게 콜백

# 실무에서는? - `NIO`

> Java AIO는 이론적으로 OS-native 비동기를 제공하지만, <br>
> 1. OS 별 구현 차이(Windows, Linux)
> 2. 제어권 부족
> 3. 복잡한 콜백 모델
> 
> 로 인해 대규모 서버 실무에서는 **Selector 기반의 NIO**가 더 널리 사용된다.
>
> 대표 사례 - Netty, Tomcat, Kafka, Redis, NGINX, Envoy 등

## 1. OS 별 구현 차이

- **Windows** 에서는 **IOCP**가 활용되어 최대치의 성능과 OS 주도 비동기가 진행되나
- **Linux** 에서는 **epoll** 기반이며 내부적으로는 NIO와 구조적으로 크게 다르지 않음.
- 즉, **플랫폼마다의 동작에 대한 일관성이 깨지기 때문**에 꺼려짐

## 2. 제어권 문제 (Backpressure, Flow Control)

- AIO
  - IO 요청을 OS에게 던진 순간, `얼마나 쌓였는지`, `언제 완료될지`에 대한 모든 주도권을 잃게 됨
  - 따라서 콜백을 수행하는 `CompletionHandler`가 한꺼번에 몰려서 실행될 수 있으며, 스레드 풀 포화 위험이 있어
  - **제어가 매우 어려워짐**
- NIO
  - 언제 읽을지를 애플리케이션에서 직접 결정 가능해 **제어가 용이함**

## 3. 복잡한 롤백 모델

- AIO
  - `I/O 완료 -> 콜백 -> 비즈니스 로직 -> 또 I/O -> 콜백 ...`
  - 상태 관리가 어렵고 트랜잭션 경계가 불명확해질 수 있음
- NIO
  - 이벤트 루프 안에서 상태 머신으로 처리되기 때문에 흐름이 명확
