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
  
