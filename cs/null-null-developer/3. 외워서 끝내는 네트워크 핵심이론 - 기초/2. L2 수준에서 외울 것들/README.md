# L2 수준에서 외울 것들

### NIC

- Network Interface Card. 흔히 LAN(Local Area Network) 카드이다.
- NIC은 H/W이며, **MAC 주소**를 갖는다.
- L2 수준에서는 `Frame` 이라는 단위로 데이터가 유통된다.
  - 1514 byte 정도의 크기 

### L2 Access Switch

- End-point와 직접 연결되는 스위치
- **MAC 주소**를 근거로 스위칭한다.
- `L2 Distribution switch`는 L2 Access swtich를 위한 스위치이다.
  - 보통 VLAN(Virtual LAN) 기능을 제공한다. 
  - `End point` -- `L2 Access Switch` -- `L2 Distribution switch` -- `Router` -- ..
    - <대략적인 이해>
      - End point는 사무실 자리마다 있는 PC
      - L2 Access Switch는 각 사무실에 있는 서버렉에 있음
      - L2 Distribution Switch는 한 층에 1개씩 정도 있음
      - Router는 건물에 1개씩 정도 있음

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/193f87ed-dca3-49f2-b1a8-a0f022615680" alt="adder" width="60%" />

### LAN, WAN, Broadcast

- Broadcast 주소라는 매우 특별한 주소가 존재한다. (MAC, IP 모두 존재)
  - 모두 1인 주소를 가짐. (MAC 예 - FF-FF-FF-FF-FF-FF)
  - 네트워크의 목적지가 위와 같은 주소라면? 모두 다 받으라는 뜻!
  - 네트워크 안에서 Broadcast가 일어나면, 다른 네트워크가 그 동안 쉬게 되어 전반에 큰 성능저하를 야기한다.
  - 따라서 범위를 최소화하는게 매우 중요하다.
- 규모에 따라 LAN - MAN - WAN 으로 점차 넓어진다.
  - 일반적으로(이해하기 쉽게 하기 위해서, 완벽한 구분과 정답이 아님) 
    - H/W(Physical) 단계(L1 ~ L2) 에서는 `LAN`
    - S/W(Logical, Virtual) 단계(L3 ~ ) 에서는 `WAN` 으로 이해해보자.
    - 즉, L1 ~ L2 Layer 에 위치한 `LAN` 은 물리적으로 연결되어 있는지를 의미하는 범위이고, <br> 
      L3 Layer 에 위치한 `Internet - WAN`은 `LAN(물리적인 연결)` 위에 존재하는 논리적인 네트워크 환경인 것이다. 

        <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/d8d835cd-ba17-4870-9b1a-463dab7ee71b" alt="adder" width="60%" />