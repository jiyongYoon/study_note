# 인터넷 공유기의 작동원리

## NAT(Network Address Translation)

- 일반적인 인터넷 공유기는 NAT 기술이 적용된 장치
- Private IP를 사용해서 하나의 Public IP를 공유할 수 있도록 주소 변환을 해줌
- 패킷 필터링 방화벽과 비슷한 보안성을 제공함

## 구조에 따른 구분

- Symmetric NAT
  - TCH 세션마다 외부 포트 지정
- Cone NAT
  - Host 단위로 외부 포트 지정

### Symmetric NAT

- 공유기인 NAT Gateway는 Inbound 쪽으로 Private IP 인터페이스, Outbound 쪽으로 Global IP (Public IP) 인터페이스를 가지게 된다.
- Private IP의 주소 범위는 아래와 같다. 이 Private IP 주소 범위는 Public에서는 존재할 수 없다.
  - 10.X.X.X (Prefix - 10.0.0.0/8)
  - 172.16.X.X (Prefix - 172.16.0.0/12)
  - 192.168.X.X (Prefix - 192.168.0.0/16)
- Packet 의 구성에서 [ IP / TCP / Payload(Data) ] 이렇게 구분한다고 하면, `IP`, `TCP` 헤더 쪽의 데이터를 바꿔치기 하는 동작방식을 취한다.

- 공유기 내부에 192.168.0.10 호스트가 15.15.15.15 웹서버와 통신한다고 가정한다면

  <img src="https://github.com/user-attachments/assets/9047f98f-49ce-4711-a6e2-90aa1a95b9f5" alt="adder" width="100%" />

  - 192.168.0.10 은 본인으로부터 출발하는 Packet을 만들어서 NAT Gateway(공유기)에 전달한다.

  <img src="https://github.com/user-attachments/assets/4c113f7d-ff31-4051-b7fb-2af326f0aa84" alt="adder" width="100%" />

  - 공유기가 Outbound로 패킷을 보낼 때, Packet의 Header를 본인으로부터 출발하는 것으로 바꾸고, 해당 정보를 `NAT table`에 저장한다.
  - 요청을 받은 15.15.15.15 웹서버는 공유기가 설치된 Public IP인 3.3.3.3 으로부터 요청이 온 것으로 인지하게 된다.

  <img src="https://github.com/user-attachments/assets/2479c07f-e74a-4580-a9e2-62b6f13700d4" alt="adder" width="100%" />

  - Response가 올 때도 마찬가지다. Web Server는 3.3.3.3 에 패킷을 보내게 된다.
  - 이를 받은 공유기는 (Remote IP, Remote Port, Protocol) 의 쌍으로 Local을 식별한 후, 주인에게 데이터를 전달한다.

  > 이러한 과정으로 하나의 Global IP(Public IP)로 공유기 내부에 있는 여러 호스트들이 통신이 가능해진다.
  
### Full Cone NAT

- 사설망 내의 Host(Local ip address + Local port) 와 공유기(NAT)간에 NAT table에는 데이터가 그대로 추가된다.
- 하지만 Outbound 쪽으로는 Remote ip address나 Remote port는 매핑이 없다. (Any ip, port 가능)
- Table로 표시하면 아래와 같다.
  
  <img src="https://github.com/user-attachments/assets/739d870c-ded3-4f5c-96ae-7431955ce92c" alt="adder" width="60%" />
  
  - 이러한 바인딩 상태에서 Public에서는 공유기 ip address + port(8080) 으로 보내게 되면, 바로 `192.168.0.10:3000`으로 연결되게 된다.
  - 데이터를 받는 Host에서 어디서 오는지 Remote를 따로 식별하지 않기 때문이다.
- 구조상 외부에서의 연결성이 훨씬 편리하다. 

> 정리하면
> 
> `Symmetric NAT` 방식은 `host ip + port` -> `External port` -> `Remote ip + port` 로 순차적으로 매핑되어 식별되며, <br>
> `Full Cone NAT` 방식은 `host ip + port` -> `External port` 로만 매핑된다.

### Restricted Cone NAT

- Full Cone 방식에서 보안성을 조금 챙기기 위해 나온 방식이며, `ip`, `port` 두 가지 방식이 있다. 보통 `ip` 방식이 사용된다.
- Full Cone 방식에서 `Remote ip`가 추가로 식별된다.
  
  <img src="https://github.com/user-attachments/assets/67d68cea-c223-4f2e-b337-446ce67bd33b" alt="adder" width="60%" />
  
  - 즉, 공유기와 통신을 한 `Remote Host`까지만 신경쓴다는 뜻이다.

### Port Restricted Cone NAT

- Restricted Cone에 추가로 `port`까지 식별하는 방식이다.
  
  <img src="https://github.com/user-attachments/assets/37600ae4-5079-4b2d-b6ac-2ffbeb791db1" alt="adder" width="60%" />

  - 이 상태에서 Local host 하나가 같은 Web server에 추가로 패킷을 보내면 NAT table에 데이터가 추가될텐데, 이 때 `External Port`는 동일하게 추가된다.
  - Symmetric NAT와 다른점은, 공유기에서의 `External Port`가 최대한 아껴서 더 열리지 않고 유지된다는 점이다.

## 포트 포워딩

- NAT table을 직접 수정하는 원리다. 즉, 외부에서 공유기로 접근할 때의 접근 `규칙`을 직접 정한다는 뜻이다.
- 단, 게임이나 RTC, 토렌트 등과 같이 유저가 신경쓰지 않아도 되도록 포트 포워딩이 되어야 하는 경우가 필요하다!

## UPnP(Universal Plug n Play)

- 공유기 대부분이 해당 기능을 지원하며, 기본적으로 실행하는 것이 default 값으로 되어있다.
- 포트 포워딩 설정이 자동화 된다. (`SSDP` 프로토콜)