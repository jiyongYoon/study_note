# VPN과 네트워크 보안 솔루션

## VPN 기술

- `Virtual Private Network`
- Public Internet 망을 `논리적`으로 나눈 Network 망
  - Private Network를 사용하면 문제가 없지만, 외부 Public 망보다 속도 및 비용 측면에서 비효율적이기 때문에 Public 망을 사용하면서 Private 함도 챙기는 효율적인 방식이다.

## IPSec

- IP-Security
- 네트워크에서의 안전한 연결을 설정하기 위한 통신 규칙 또는 프로토콜 세트
- `패킷(L3)` 단위에 적용됨
  - IPv4, IPv6를 모두 지원
  - 응용 프로그램에 대한 의존성이 없고 IP 기반 통신을 모두 보호할 수 있음
- GtoG VPN 구현을 위해 가장 많이 사용되고 있는 방식

### IPSec Protocol

- `암호화 키` 및 `보안 규칙 협상` 관리
- `데이터 원본인증 및 무결성` 제공 -> Hash 사용
- `기밀성` 제공 -> PKI 기술 사용

## VPN Tunneling

<img src="https://github.com/user-attachments/assets/9a2b0de4-d151-43cb-ac7a-a685d0b9655d" alt="adder" width="80%" />

- 마치 터널(Tunnel)에 들어가면 외부에서는 터널 내부를 더 이상 감시할 수 없는 것처럼, 암호화 되어 보지 못하는 것을 뜻함
- 기존 `Packet` 덩어리(`IP Header` + `Payload`)를 암호화하며, 새로운 `IP Header`를 추가하여 전송함
  - 배송 박스를 새로운 박스로 한겹 더 포장하는 느낌쓰

### VPN GtoG

<img src="https://github.com/user-attachments/assets/2784bd41-3231-4ad4-ba61-1df7180f5be5" alt="adder" width="100%" />

- Public 구간에서 Packet의  `암호화` 및 `IP Header 재포장` 을 사용하여 접근 제한 및 기밀성을 유지함
- 그림에서 `2`, `3`번 Gateway가 암-복호화 및 IP Header 관리를 하게 된다.
- `4` 서버는 `2` Gateway의 ip 대역대의 접근만을 허용하면 `2` Gateway 하위에 있는 어떤 Endpoint에서 접근하던 상관이 없을 것이기 때문이다. (보안 정책을 더 타이트하게 유지할 수도 있음) 

<img src="https://github.com/user-attachments/assets/578a9060-15ab-46c8-bbf3-9934f2f243ea" alt="adder" width="100%" />

- 실제로 우리가 사용하는 VPN이 동작하는 방식의 그림
- `VPN Client` 프로그램을 사용하게 되면, `IP` 가 2개가 생기게 된다.
  1. 기존 내 Gateway의 IP
  2. VPN Client로 제공받는 IP

### 그림으로 보는 VPN과 재택근무의 동작 흐름

1. VPN Client를 동작시킨 후 HTTP 요청을 생성하여 보내면, `VPN Client로부터 할당 받은 IP가 Inner IP Header`에 찍히며, 이 `Packet이 암호화` 된다. 암호화 된 Packet은 내 `Public IP(9.9.9.1)를 Outer IP Header`로 달고 VPN Client의 목적지(3.3.3.1)로 보내진다.
2. 목적지에 도착한 패킷은 기존의 Outer IP Header를 지우고 내 Public IP로부터 진짜 목적지의 IP로 가는 새로운 Outer IP Header로 갈아끼워진다.
3. 진짜 목적지의 Gateway는 해당 패킷의 Outer IP Header를 지우고 진짜 Packet을 복호화하여 목적지 서버에 전송한다. 즉, 목적지 서버는 `VPN Client IP Address로부터 요청이 왔다고 착각`하게 된다.
4. 응답은 1 ~ 3 번 과정의 역순으로 진행된다.

- 정리하면
  - End to End 에서는 `Inner IP Header`를 사용하며 `VPN Client 할당 IP <-> 최종 목적지 Server IP`
  - Public Internet 구간에서는 `Outer IP Header`를 사용하며 `Gateway IP <-> Gateway IP`

## 네트워크 보안 솔루션 종류

- 당장 급하게 공부할 것은 아니기 때문에, 보안 솔루션 별로 어떤 네트워크 장치의 구조를 사용하는지 정도만 이해해보자. 그러면 필요 시 큰그림을 잘 그릴 수 있게 될 것이다.

### 종류와 분류

|  **솔루션종류**  |  **Inline**  |  **Out of path**  |  **Proxy**  |
|:-----------:|:------------:|:-----------------:|:-----------:|
|    PC방화벽    |      V       |         V         ||
|     NAC     |      V       |         V         ||
|     방화벽     |      V       |||
|     IPS     |      V       |||
|    NIDS     ||      V       ||
|     UTM     |      V       |         V         |      V      |
|     VPN     |      V       |||
|   SSL VPN   |      V       |||