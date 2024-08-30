# TCP/IP 프로토콜에서의 Socket, Port, Connection

<img src="https://github.com/user-attachments/assets/cebb6a8a-936d-414b-8db7-17181232cdb8" alt="adder" width="40%" />

TCP/IP stack (IETF에서 RFC 문서로 관리하는) 을 기준으로 좀 더 현실적인 stack에서 socket, port, tcp connection을 연결지어 이해해보자.

---

## TCP/IP stack의 두 구분

- application layer
  - 애플리케이션 레벨에서 구현 및 관리가 되며, 네트워크 기능을 `사용`하는 것에 목적이 있음
- transport, internet, link layer
  - 하드웨어/펌웨어, OS 레벨에서 구현 및 관리가 되며, 네트워크 기능을 `지원`하는 것에 목적이 있음

> Port는 이 두 큰 덩어리 사이에 존재하여 통로 역할을 한다.
> > [Port에 대한 RFC 문서 내용] <br>
> > The portion of a socket that specifies which logical input or output channel of a process is associated with the data. <br>
> > 프로세스의 논리적 입력 또는 출력 채널이 데이터와 관련이 있는 소켓의 일부.

## IP (Internet Protocol)

- 출발 Host에서 목적지 Host까지의 데이터를 주고 받는 역할을 하는 Protocol
- 기본적으로 신뢰할 수 없다. (데이터가 유실되고 순서가 보장되지 않음)
- 그러나 데이터를 사용하는 입장에서는 데이터를 안정적으로(유실 없이 순서 그대로) 받을 수 있어야 한다.
- 때문에 상위 Layer에서 그러한 프로토콜을 정의하여 사용한다. -> TCP (Transmission control protocol)

## TCP (Transmission Control Protocol)

- connection-oriented protocol (연결 지향)

### Connection

- 프로세스간의 안정적이고 논리적인(Virtual) 통신 통로
- 통로를 열 때 `3-way handshake`가 일어남
- 데이터를 주고받음
- 통로를 닫을 때 `4-way handshake`가 일어남

> 상대방 애플리케이션의 port를 알아야 찾아갈 수 있겠다

### Port (Number)
- 16 bits로 이루어진 숫자 (0 ~ 65535)
- 0 ~ 1023: well-known ports, system ports 
  - e.g.) HTTP(80), HTTPS(443), DNS(53)...
- 1024 ~ 49151: registered ports (IANA에 등록된 번호)
  - e.g.) MySQL(3306), Apache tomcat server(8080)...
- 49152 ~ 65535: dynamic ports

> public internet 망에서는 상대방을 알아야 찾아갈 수 있겠다

### Internet Address
- Host를 유니크하게 식별하는 숫자
- 요즘은 흔히 ip address 라고 부름


### Socket
- 최종 목적지 (Host + Application)를 고유하게 식별할 수 있는 개념이 바로 `Socket`이 된다. 즉, Socket은 ip address + port number 라고 생각할 수도 있겠다.

### Connection 과 Socket

각 Connection은 유니크하게 식별이 되어야 한다. `출발지 <-> 목적지` 라는 개념은 한 가지가 되어야 하기 때문이다.
우리가 택배를 받을 때, 그 택배가 `출발지로부터 목적지까지 오는 배송` 이라는 개념은 유니크하게 식별이 된다. 하나의 액션으로 볼 수 있을 것이다. 이 개념적인 것을 현실세계에서 어떻게 구별할 수 있을까?
바로 `보내는 곳 + 받는 곳` 한 쌍으로 식별할 수 있을 것이다. 

이를 서버끼리의 통신으로 다시 가지고 온다면 보내는 곳과 받는 곳 모두 `Socket(ip address + port number)`이 되기 때문에, 한 `Connection`은 `Socket의 쌍`으로 고유하게 식별이 가능하게 된다.
여기서 보내는 곳은 `Source Socket`, 받는 곳은 `Destination Socket`이라고 부르기도 한다.

## UDP (User Datagram Protocol)

- connectionless protocol
- 연결을 맺지 않고 바로 데이터를 주고 받는다. 순서 보장, 데이터 보장 등에서 관심이 없다.
- 따라서 Internet Protocol을 거의 그대로 사용하게 된다.

> Socket이라는 개념이 TCP 안에 있을때는 IP + PORT로 식별이 가능했으나, UDP 까지 확장되어 사용되면서 TCP/UDP 인지 구분하는 protocol까지 식별하는데 포함되게 된다.
> 즉, `Socket = protocol + ip address + port number`라고 보면 될 것이다.

**실제 프로그래밍에서 Socket은 어떻게 동작을 할까?**

## Socket

- 애플리케이션에서 System을 사용, 특별히 여기서는 Network 기능을 사용하도록 하기 위해서 `인터페이스`를 제공하는데, 이 인터페이스를 `소켓`이라고 이해해도 무방할 것이다.
- 즉, 개발자는 `소켓`을 사용하여 애플리케이션의 데이터를 하드웨어 시스템을 통해 외부로 보내게 된다.
  - 이 소켓은 라이브러리나 모듈, 프레임워크단에서 이미 구현이 되어 있는 경우가 많아, 실제로 소켓을 열어서 네트워크 통신 기능을 구현할 일은 거의 없기는 하다.
  
  <img src="https://github.com/user-attachments/assets/ab2004df-d6df-44df-9b00-82fbcbdc335d" alt="adder" width="50%" />

  - springboot `package org.apache.tomcat.jni;` 위치의 소켓 생성 메서드를 보면 이런식으로 구현이 되어 있다.

  <img src="https://github.com/user-attachments/assets/70d77dc1-9bf1-4533-8503-fe52eb0aa49c" alt="adder" width="70%" />
  
  - 또 이런식으로 ip(family), protocol, 그리고 이후에 port 등을 추가하여 소켓이 생성된다. 

- 단, 실제 구현에서는 `TCP socket은 protocol + ip address + port number로만은 고유하게 식별되지는 않는다.`

### TCP에서 사용하는 Socket의 실제 구현

- 서버에서는 항상 Listening Socket이 열려있다.
- Client가 해당 Socket에 연결 요청을 하면 Server는 새로운 Socket을 열어 실제로는 그 Socket과 Client를 연결하게 된다. (`바인딩 과정`)
- 그렇다면 추가로 만들어진 새로운 소켓은 어떤 Port를 가지게 되는가? => `동일한 ip address, port number`를 가지게 된다. => 그럼 어떻게 식별하나?
  

> **Client의 요청은 2가지 종류가 있다. 서버는 그 종류마다 다른식으로 처리를 하게된다.**

<img src="https://github.com/user-attachments/assets/1cb4dbbf-f3b2-4f5d-b96c-c49da59dc053" alt="adder" width="50%" />

위와 같이 Client에서 데이터가 오게 되면 아래처럼 두 가지로 구분할 수 있게 된다.

1. `최초 연결`을 위한 Client 요청 -> Listening Socket으로 받음! -> 새로운 Socket 생성 및 바인딩
2. `이미 연결`된 것으로 Data를 보내는 요청 -> 출발지의 Socket(protocol + ip address + port number)로 바인딩 된 Socket을 찾은 후, 해당 Socket으로 데이터를 받음!

---

- 참고자료: [쉬운코드 유튜브](https://youtu.be/X73Jl2nsqiE?si=NNGuzRei1jgs4KZp)
