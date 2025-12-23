# NIO와 AIO의 차이를 확인하는 관점

> I/O 완료를 누가 책임지고, 누가 깨어나는가?

## NIO (Selector 기반) ─ `Ready-based I/O`

```text
[Java Thread]
    |
    | selector.select()
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
    | async write 요청
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
- 실제 I/O 수행 및 완료까지 OS가 책임
- 완료되면 OS가 JVM에게 콜백