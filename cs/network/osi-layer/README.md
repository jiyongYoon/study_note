# Network Layer

네트워크 통신이 진행될 때, 공통적이고 범용적인 패턴을 가지게 된 것을 `기능별로` 구조화 한 것.

대표적으로 `OSI 7 Layer`, `TCP/IP stack(4 Layer)` 등이 있다. 가장 쉽게 접할 수 있는 `OSI 7 Layer`을 살펴본다.

---

## OSI 7 Layer

<img src="https://github.com/user-attachments/assets/2c32e46c-01e3-4c30-9769-a4f470a3a45e" alt="adder" width="100%" />

- 기본적으로 각 레이어는 하위 레이어의 기능을 사용하여 본인의 레이어에서의 역할을 구현하게 된다.

- `L7 - Application Layer`
  - 애플리케이션 목적에 맞는 통신방법 제공
  - HTTP(Web Data..), DNS(IP <-> Domain Name), SMTP(메일), FTP(파일) 등
  - 애플리케이션 목적에 맞는 통신방법에만 관심이 있고, 이 데이터가 어떻게 전송되는지는 관심사가 아님
- `L6 - Presentation Layer`
  - 애플리케이션 간 통신에서 메시지 포맷 관리
  - 인코딩 <-> 디코딩, 암호화 <-> 복호화, 압축 <-> 압축 풀기 등
- `L5 - Session Layer`
  - 애플리케이션 간 통신에서 세션 관리
  - RPC
> `L7`, `L6`, `L5` Layer는 모두 `애플리케이션`에 대한 내용이 관심사
- `L4 - Transport Layer`
  - 목적지 애플리케이션 데이터 전송을 위해 어떤 방식의 통신을 할 것인지
  - TCP(데이터 전송 보장) / UDP(필수 기능만 제공)
  - 목적지를 어떻게 찾아갈지는 관심사가 아님
- `L3 - Network Layer`
  - 출발지 호스트로부터 목적지 호스트로의 데이터 전송
  - IP 주소를 활용
  - 네트워크 간 최적의 경로 결정
  - 목적지를 표현하는 것에 관심이 있지 어떻게 목적지까지 갈지는 관심사가 아님
> `L4`, `L3` Layer는 `목적지 식별`하는 것이 관심사
- `L2 - Data Link Layer`
  - 출발지 호스트로부터 목적지 호스트까지 어떻게 갈 것인지
  - MAC 주소 기반 노드간의 통신 담당
  - ARP: IP 주소 <-> MAC 주소
- `L1 - Physical Layer`
  - bits 단위로 데이터 실제로 전송
> `L2`, `L1` Layer는 목적지까지 `어떻게(어떤 경로, 어떤 형태) 갈 것인지`가 관심사

## Router

- 인터넷 망을 이루는 핵심인 `Router` 들은 들어온 데이터를 어디로 보내는지 식별하여 중간 노드 역할을 하는 것이므로 `L1`, `L2`, `L3` Layer까지 구현하면 된다.

---

- 참고자료: [쉬운코드 유튜브](https://youtu.be/6l7xP7AnB64?si=o0R-QuO8WBegtfPc)