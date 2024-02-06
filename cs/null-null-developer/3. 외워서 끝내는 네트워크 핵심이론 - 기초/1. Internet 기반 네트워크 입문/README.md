# Internet 기반 네트워크 입문

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/1cc7eff6-7afe-4cdb-89af-f88b4f0b862a" alt="adder" width="60%" />

### Layer?

- 계층 구조다.
- 이는 위의 Layer가 아래 Layer를 의존하는 '관계'를 도식화 하기 위함이다.

### OSI 7 Layer, 나중에 깊게 공부하자

- Network를 학습할 때 꼭 나오는 개념인 `OSI 7 Layer`
- Layer 구조로 되어있다는 말은, 윗 단계가 아래 단계의 행위 또는 존립에 대해 의존적이라는 것을 나타낸다고 했다.
- `OSI 7 Layer`도 마찬가지이다. 그러나 이는 대단히 `개념`적인 것이기 때문에, 네트워크 초반에 학습하기에는 다소 추상적이고 이론적이다.
- 조금 더 쉽게 다가간 후에, 네트워크의 개념을 도출하는 식으로 학습해보자!
> 마치, 백엔드 학습을 할 때, Java 언어를 배울때, 언어를 배우기 위해 언어의 개념과 컴퓨터 동작방법부터 시작하는 꼴이군.<br>
> 틀린건 아니지만, 매우 어려운 방법일 것이다.

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/35357e95-d8a5-43b3-9c34-a48eee4a5289" alt="adder" width="60%" />

### OSI 7 Layer의 대표적인 구현체와 식별자

- 각 단계별로 대표적인 구현체와 해당하는 식별자들이 있다.
- 특히 중요한 3가지를 외우자.

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/d8d835cd-ba17-4870-9b1a-463dab7ee71b" alt="adder" width="60%" />

  | Layer  |      구현체      |                              식별자                               |
  |:--------:|:-------------:|:--------------------------------------------------------------:|
  | L1 - L2 | Ethernet(이더넷) |                        MAC주소 - NIC을 식별                         |
  | L3     | Internet(인터넷) |                     IP주소(v4,v6) - Host를 식별                     |
  | L4     |   TCP, UDP    | Port(포트) - Process, Service, Interface 등 <br> 다양한 레벨에서 식별자로 사용 |

### Host

- 네트워크(인터넷)에 연결된 컴퓨터를 지칭한다.
- 그 중 Network **그 자체를 이루는 host**를 `Switch` (인프라 구성요소)
  - ex) Router, IPS, Tab, Aggregation 등
- 그 중 Network **이용 주체**를 `End-point`라고 함
  - ex) Client, Server, Peer 등의 단말기

### Switch가 하는 일

- 패킷(정보의 단위)이 출발지 -> 목적지 의 여정에서 만나는 교차로를 `Switch(Interface)`라고 한다.
- 경로를 '선택'하는 작업을 `Switching`이라고 한다.
- **경로를 선택할 때는 어떤 근거가 있을텐데?!**
  - IP주소를 가지고 선택을 한다면 => L3 Switch => `Router`
    - Router는 `Routing Table`을 가지고 있다. (목적지가 어디면 어디로 가라-)
  - Mac 주소를 가지고 선택을 한다면 => L2 Switch
  - Port 번호를 가지고 선택을 한다면 => L4 Switch
  - HTTP 프로토콜을 가지고 선택을 한다면 => L7 Switch
  - 이런식이다.
- 교차로에서 선택 시 비용을 `Matric` 이라고 한다.
  - 적은 비용의 경로를 선택하는게 필요하다.