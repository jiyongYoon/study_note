# 네트워크 장치의 구조

## 세 가지 네트워크 장치 구조

네트워크와 그 장치에 대해 학습할때는 이 세가지 중에 어디에 속하는지, 또는 어느 특성들을 가졌는지를 먼저 따져보는 것이 좋다.
카테고리화 해놓으면 이해가 쉽기 때문이다.

1. Inline
2. Out of path
3. Proxy

### Inline
- `Packet` + Drop(차단)/Bypass(허용) + Filtering
- 패킷이 `통과`해서 가는 장치
- 대표적인 장치: Router

  <img src="https://github.com/user-attachments/assets/08a0b509-6fc3-4ab0-8459-5a2c8cf859c5" alt="adder" width="80%" />

- Router도 결국 `LAN 카드가 여러개 달린 컴퓨터`라고 생각하면 되는데, 이 라우터를 중심으로 Public 망쪽이 `Outbound`, 단말기 쪽이 `Inbound`가 된다.
  - 컴퓨터에 A, B 2개의 LAN 카드가 달려있다고 생각하면, A로 택배(Frame 단위 데이터)가 들어오고 B로 택배가 나가게 된다.
  - 여기서 단말 서버들과 다른 지점은 해당 데이터는 내가 생산하거나 받을 데이터가 아니라는 점이다.
  - 따라서 Kernel mode에서 `Inline` 장치를 통해 Outbound 쪽으로 나가게 된다. 
    - 속도 면에서도 굳이 User mode까지 올라올 필요가 없다.

### Out of path
- `Packet` + Read only, Sensor(인지)
- 패킷을 읽고 인지만 하는 장치 -> 분석, 탐지의 목적
- 대표적인 장치: 여러가지 Sensor
  - 패킷 수집장치가 필요함
    - L2 Port Mirroring: 다른 포트 하나로 데이터를 모두 복사해서 전달함
    - Tab 스위치: 

### Proxy
- `Socket Stream` + Filtering
- 스트림 데이터를 볼 수 있다는 것이 큰 특징
- (User mode의) Application 레벨에 있으며 그렇기 때문에 Kernel mode의 Packet이 전처리 된 후 Socket을 통해 올라오게 된 상태다.
- OSI 5~7 계층을 커버하게 됨
  - HTTP는 L7 레벨이기 때문에 Proxy에서 다루는 것이 유리하다는 뜻이 된다.
