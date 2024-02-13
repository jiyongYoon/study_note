# L3 수준에서 외울 것들

---

### IPv4 주소의 기본 구조

- 8bit * 4 = 32bit
  - 8bit = 2^8 = 256. 즉, 0 ~ 255까지의 범위가 가능하다.
- 우리가 흔히 접하는 ip주소의 형태 `192.168.0.2`
  - . 으로 구분되며, 3번째 범위(24bit)까지가 `Network ID`, 그리고 마지막이 `Host ID` 구간이다. 
- 해석하면 `어떤 네트워크`에 연결된 `호스트 번호`를 식별할 수 있게 된다.

### L3 Packet

- 네트워크의 정보데이터 단위
  - 송장이 붙은 택배 박스라고 생각하자.
- 최대 크기는 `MTU` = 1500 bytes (1.4x KB)
  - `Header`(송장) + `Payload`(내용물) 로 구성됨.

### Encapsulation과 Decapsulation

- 캡슐화
  - 어떤 것을 `단위화` 했다는 의미
  - 무엇이 들어있는지 보이지 않게 `감싼다`는 의미

  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/cd9a526f-b5d2-4a4c-964f-bec0cde17611" alt="adder" width="80%" />

### 패킷의 생성과 전달

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/73205b88-fc4f-4857-8832-b2d8af3f3689" alt="adder" width="80%" />
<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/b6ea702d-ac89-47e4-834a-1e19fbee6f31" alt="adder" width="80%" />
<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/5219e40b-1fc0-42d7-aefa-d39436b36fe4" alt="adder" width="80%" />

### TCP/IP 송-수신 구조

- File Download 상황을 가정해보자.
  - Server로부터 1.4MB 크기의 파일을 다운로드 받는다고 가정하면, MTU 최대 크기가 1.4xxKB이기 때문에 약 1000여개의 패킷으로 쪼개져서 전송되게 된다.

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/d2327a71-9dc4-424f-809f-b63e35a723cd" alt="adder" width="100%" />

**송-수신 과정**

[송신]
1. HDD에 있는 File을 Process 메모리 버퍼에 COPY한다.
2. Network로 전송하기 위한 File(Socket) 입출력을 시작한다. 이 과정에서의 데이터 단위는 `Stream`이다. `Stream`은 데이터의 크기보다는 형태에 대한 개념이며, 파일 읽기 시작 ~ 끝 을 의미한다.
3. L4 TCP 스텍을 만나게 되면 분해를 시작한다. 이 과정에서의 단위는 `Segment`이며, OS 단위에서 진행하게 된다. <br>
   L3 IP 스텍을 만나게 되면 포장을 시작한다. 이 과정에서의 단위는 `Packet`이며, 송장(`Header`)이 붙게 된다.
4. L2 Layer에서 NIC Driver를 통해 Packet이 전송되며, 이 과정에서의 단위는 `Frame`이다. `Frame`은 네트워크 환경과 상황에 따라 유동적으로 수시로 변하게 된다. <br>

[수신]
5. 클라이언트의 NIC 을 통해 `Frame`에서 Packet이 튀어나온다.
6. L3 Layer에서 `Packet`이 소멸되고 정보인 `Segment`가 튀어나온다.
7. L4 Layer에서 파일 입출력을 하고 있던 Socket 버퍼로 데이터인 `Segment`를 담는다.
8. 실제 사용하는 Process의 메모리 버퍼에서 Socket 버퍼에 있는 데이터를 가져온다.

**장애상황**
- Loss(데이터 유실): 어찌된지는 Internet에게 물어봐야함. (`network` 이슈)
- Re-transmission & ACK Duplicate: (`network`, `end-point` 이슈) 
  - Server에서 패킷을 포장하여 보낼때, 1개만 보내지 않고 여러개를 한번에 보내고나서 잠깐 Wait를 하게 된다.
  - 수신자로부터 오는 `ACK` 신호를 받기 위해서인데, 여기에는 `다음에 받을 패킷 번호`와 `여유 공간` 정보가 있다.
  - 이를 통해 다음에 보낼 패킷을 인터넷으로 다시 전송하게 되는데, 기다려도 `ACK`가 오지 않으면 동일한 패킷을 다시 보내게 된다.
  - 이 과정이 짧은 순간에 일어나면, 
    - 송신자는 `ACK`가 오기 전에 동일한 데이터를 다시 보내게 되며(`Re-transmission`)
    - 수신자는 동일한 데이터에 대해 동일한 `ACK`를 다시 보내게 된다(`ACK Duplicate`)
- Out of order: 패킷의 순서가 맞지 않거나, 중간 패킷이 없는 경우 (`network` 이슈)
- Zero window: 수신측이 보내는 `ACK` 응답에 담기는 `여유 공간`이 부족한 경우다. (`end-point(APP)` 이슈)
  - 수신 측에서의 7 -> 8 번 과정에서, 어떤 이유에서든지 데이터를 사용할 Process가 socket 버퍼의 segment를 가져가는 속도가 느리면 발생하게 된다.

### IPv4 Header 형식

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/060072f8-5c72-4c45-8b46-8eab7a28519d" alt="adder" width="60%" />

- Wireshark로 IPv4 Header를 확인한 내용 

  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/8e67a960-2757-448d-8b84-aaa2b8b1809a" alt="adder" width="60%" />

  - Version: `4` -> IPv4를 나타냄
  - IHL: Header Length: `5` (5행, 즉 4 bytes * 5 = 20 bytes를 나타냄) 
  - TOS
  - Total length: 총 패킷의 길이. 이론상 2^16인 64KB까지 가능하나 실제로는 MTU 사이즈로 운용된다.
  - Identification
  - Flags, Fregment offset: 단편화 관련(MTU로 자르는것 관련)
  - TTL: Time to Live. 네트워크에서 이동 단위인 홉(hop)을 지날때마다 1씩 줄어들며, TTL 값이 모두 지나면 해당 패킷은 소멸된다. (패킷이 영영 인터넷을 떠도는 것을 방지)
  - Protocol: Payload에 다시 Header가 오는 경우, 어떤 방식으로 해석할 것인지 명시
  - Header checksum: 보안성은 없는 패킷 손상 관련 체크썸
  - address: 출발지, 목적지가 있음

### Subnet Mask

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/79bcd1f2-0088-4fe0-af54-8aefc2bfa868" alt="adder" width="60%" />

- `1111 1111` `1111 1111` `1111 1111` | `0000 0000` 으로 기존 주소를 Mask 연산(bit별 AND 연산)을 하면 마지막 자리인 `Host ID`를 제거한 `Network ID`주소를 얻을 수 있다. 
- 이러한 용도로 사용되던 것이 Subnet Mask였다.
`=> 그러나 요즘은 컴퓨터가 너무 좋아졌다.`
- `CIDR 표기법` 

  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/3db76d31-4c97-44f1-a792-4c2cb811f38e" alt="adder" width="40%" />

  - 이 부분을 `Network ID` 라고 하자고 명시하는쪽으로 변하게 됨. (이는 관리적인 측면에서 보다 효율적이고 직관적이다.)

### Broadcast IP 주소

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/63508e17-8594-4881-8607-e30a0a797022" alt="adder" width="60%" />

- 네트워크 내의 모든 곳으로 쏘게 됨
- 따라서 네트워크 부하가 필연적으로 생김!

### Host 자신을 가리키는 IP 주소

`127.0.0.1`

- 내가 나에게 `접속`, `연결`한다?
  - 나의 `Process` 간의 통신을 뜻한다! => `IPC 통신`
  
  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/2f54d751-a5c1-4eb1-8d08-633d436aae49" alt="adder" width="60%" />
