# L4 수준 대표주자 TCP와 UDP

---

### TCP

- 근본적으로 논리적인 연결이다. (Virtual)
- TCP에만 연결(Connection, Session) 개념이 있다.
- 연결은 결과적으로 `순서번호`로 구현된다.
- 연결은 `상태(전이)` 개념을 동반한다.

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/e9da90d6-3d11-43ec-925c-5a472d8d2d7e" alt="adder" width="80%" />

- 보통 `Server`는 `Listen 상태`이며 `연결 및 연결 종료 요청의 주체`는 `Client`가 되는 것이 일반적인 상황이다.
- 서버에 TCP 연결을 시도하기 위해서 클라이언트는 서버의 IP와 TCP 연결 Port 번호를 알아야 한다.
    - 서버는 보통 TCP 연결을 위해 80번을 열어놓는다.
    - 클라이언트의 프로세스는 TCP 연결을 위해 소켓을 Open 하게 되는데, 이 때 OS에서 남는 포트 아무거나 부여를 하게 된다.

### TCP 연결과 3-Way handshaking

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/9e60f7f3-e4bc-47fd-a938-7b306ed5caf4" alt="adder" width="80%" />

- 연결 과정에서 오가는 Segment에는 TCP Payload가 없이 껍데기만 가게 된다.
- 연결 과정은 `Sequence Number`와 `정책(특히 Maximun Segment Size)`을 교환하는 것이 목적이다.
  - MSS를 알아야 서로의 MTU에 맞게 패킷사이즈를 정해서 보낼 수 있기 때문이다.

### TCP 연결 종료와 4-Way handshaking

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/31a1330c-ba20-4a44-ae2a-a34a42aed6c3" alt="adder" width="80%" />

**종료 동작 순서**
- 클라이언트의 연결 종료를 위한 FIN + ACK 발송
- 서버의 응답 1 (ACK)
- 서버의 응답 2 (FIN + ACK)
- 클라이언트의 마지막 응답 (ACK)
- 이 후 클라이언트 TCP의 상태가 `TIME_WAIT`로 변경되며, 일정 시간이 흐른 후 `CLOSED` 실제 종료가 된다.
  - 종료가 되면 Socket 및 Port 번호가 회수된다.
  - 일정 시간은 표준에서 정한 값이 있으며, 2MSL(Maximum Segment Lifetime)이다.
  - 바로 종료하지 않고 `TIME_WAIT`를 하는 가장 큰 이유는, 지연 문제 때문이다.
    - 데이터가 늦게 올 수 있을 가능성을 고려하여, 해당 소켓이 즉시 재사용되지 못하도록 막는 것.
    - 또, 마지막 ACK가 손실 될 경우, 종료 과정에 문제가 발생할 수 있기 때문.

> `TIME_WAIT` 상태의 중요성
> 
> `TIME_WAIT` 상태는 연결을 끊는 주체에서 발생을 하게 되며, 가장 마지막에 발생한다. <br>
> 보통은 `Client`가 연결 요청을 하고 연결 종료 요청을 하게 되는데, <br>
> 어떠한 이유에서 `Server`가 연결 종료를 요청하고 싶은 경우가 생길 수 있다. <br>
> 
> 이럴 때, `Server`에서 직접 연결 종료 요청을 하게 되면, `Server`에서 `TIME_WAIT` 상태가 발생하게 된다. <br>
> 그리고 이는 `Server`의 부하 및 병목지점으로 이어지게 된다.
> 
> 때문에, 어떠한 이유로 `Server`가 연결 종료 요청을 하고 싶어진다면, `Client`가 연결 종료 요청을 하도록 유도하는 구조를 가져가야 한다.
> 
> 참고자료: [sunyzero 님의 블로그](https://sunyzero.tistory.com/198)