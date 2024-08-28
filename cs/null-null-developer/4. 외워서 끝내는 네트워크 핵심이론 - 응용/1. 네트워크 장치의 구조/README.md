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
    - L2 Port Mirroring: 데이터가 들어오는 포트 외에 다른 포트 한 곳으로 데이터를 모두 복사해서 전달함
    - Tab 스위치: 수집만 전문적으로 하는 장치(복사 부하가 많기 때문에). 여러개의 포트로 동시에 복사가 가능함 
    - 국가에서 지정해놓은 차단 사이트에 접속 시 차단이 되는 경우가 이런 Sensing을 통해서 차단을 하게 됨
    - Packet을 뜯어보면
      
      <img src="https://github.com/user-attachments/assets/b4bb166d-876c-439a-80a0-fc2fb2d93844" alt="adder" width="70%" />
      
      - IP + TCP + HTTP + Data 로 구성되어 있을텐데, 우리나라는 ISP에서 HTTP를 보는 행위(SPI)까지 허용이 되어있다. Data를 보는 행위(DPI)는 안된다.
  - 패킷을 수집하면 저장을 해두었다가 나중에 분석 및 탐지를 할텐데, 이 때 Network로 들어오는 데이터 속도보다 HDD, SDD 등의 쓰기 속도는 현저히 느리다.
    - 이 부분에서 손실없이 수집하고 저장하는 것이 굉장히 고도화된 기술이다.
    - 대부분은 어느정도 비율의 손실과 가격의 적정선을 타협하는 수준이다.

### Proxy
- `Socket Stream` + Filtering
- 스트림 데이터를 볼 수 있다는 것이 큰 특징
- (User mode의) Application 레벨에 있으며 그렇기 때문에 Kernel mode의 Packet이 전처리 된 후 Socket을 통해 올라오게 된 상태다.
- OSI 5~7 계층을 커버하게 됨
  - HTTP는 L7 레벨이기 때문에 Proxy에서 다루는 것이 유리하다는 뜻이 된다.
  
  <img src="https://github.com/user-attachments/assets/48ec9c35-5874-44e1-91ba-af8fd968bf44" alt="adder" width="80%" />

- **클라이언트 관점** 
  - `Forward Proxy` (public 망 앞)
  - 프록시를 통해 우회를 하는 경우도 생기지만, `이 구조`를 사용해서 보호와 감시 용도로 사용할 수도 있다.
  - 특정 서버(혹은 외부 Public Internet 등)에 접속하기 위해서는 `특정 IP`만 가능하게 하도록 하면 이 `특정 IP`를 가진 서버가 프록시 서버가 되는 것이다.

- **서버 관점** 
  - `Reverse Proxy` (public 망 뒤)
  - 서버와 public 망 사이에서 보통 '서버를 보호하기 위한 전처리 및 로드밸런싱 등' 목적으로 사용할 수도 있다.