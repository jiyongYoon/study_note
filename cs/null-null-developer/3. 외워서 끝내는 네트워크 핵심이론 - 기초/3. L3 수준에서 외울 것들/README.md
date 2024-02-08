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
