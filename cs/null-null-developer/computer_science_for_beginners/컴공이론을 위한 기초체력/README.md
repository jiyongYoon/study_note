# 컴퓨터를 이해하기 위한 기본 단위, `bit`

> 1. 컴퓨터는 모든 정보를 `Digital`로 처리한다. (0, 1)
> 2. `1bit`는 전기 스위치 1개를 나타내며, On = 1, OFF = 0이다.
> 2. 그리고 컴퓨터는 `4bit`씩 묶어서 어떤 값들을 처리하게 된다.
> 3. 2진수 4bit는 총 2^4 = 16가지 경우의 수가 나오는데, 이걸 한번에 표시하기 위해 `16진수`를 사용한다.

## 1bit와 2진수
- 1bit란 '전기 스위치 1개'
  - 전기가 흐르는 상태 - ON - 1
  - 전기가 흐르지 않는 상태 - OFF - 0

## 스위치 4개를 조합하면?
- 4bit
  - 경우의 수는 2의 4승 => 16가지
  - 예시
    - ON - OFF - OFF - ON
    - 1 - 0 - 0 - 1 ==> 1001<sub>(2)</sub>

## 진법 변환
<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/84a1df67-7321-47e9-8a28-1c3d52251da4" alt="2-10-16" width="40%" />

- 컴퓨터는 대부분 `4bit-16진수` 포기를 하게 됨.
- Prefix로 `0x`라고 붙여주는 경우가 있음.
  - 예시) 10진수 244 = `0xF4` = 16진수 `F4` = 4bit + 4bit = 8bit
    - `0x`는 무시, F = 16, 4 = 4 이므로, F는 15 * 16^1 = 240, 4 는 4*16^0 = 4

## 16진수(4bit) 어디에 쓰이나?
### 색상표현
- RGB (0~255, 0~255, 0~255) = 8bit + 8bit + 8bit = 24bit (여기에 alpha 채널 8bit를 더하면 32bit true color)
  
  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/6e87fb74-77f1-4f9c-8b9a-566eb8c4a010" alt="rgb" width="40%" />
    
  - 빛의 삼원색 Red(255-0-0), Green(0-255-0), Blue(0-0-255)는 각각 8bit로 표현.
  - 8bit? = 2^8 = 256 = (0 ~ 255)
  - `Lime`색을 예로 든다면, `#00FF00` 이므로, RGB 순으로 8비트씩, 즉 2자리씩 끊어서 `R(00)-G(FF)-B(00)`이 되는 것.
### 컴퓨터 하드웨어 주소 표현

### 메모리 값 표현
- 예시) java 객체 주소값 (`1b6d3586`)

  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/cbcf8901-d2cd-47a1-9d68-f220c8fca7fb" alt="java-class-address" width="40%" />

- java에서 Integer의 MAX, MIN 값

  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/e6994dbb-48f7-437b-b361-75a34f069e5c" alt="max_min" width="40%">

## 8bit = 1byte

- 1 바이트(byte) 부터 용량이라는 개념이 되기 시작한다. (메모리에서 관리하는 단위가 되기도 함)
  - 1 바이트는 `영문자 한 글자`가 저장될 수 있는 메모리 크기! (한글은 2바이트(인코딩 체계에 따라 달라지기는 함))
  - 8bit = 2^8 = 256가지
  - 16bit = 2^16 = 65,536가지 (64KB)
  - 1024byte(2^10) = 1KB

## 컴퓨터 메모리 용량을 말하는 단위
<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/efe47390-1321-4e45-8952-455740f13b5f" alt="byte" width="80%">

## 컴퓨터가 글자를 다루는 방법
### 코드체계
  - 코드체계 = 숫자 하나를 글자 하나로 매핑시켜서 씁시다!

### ASCII
- 아스키코드(American Standard Code for Information Interchange)
  - 십진수 `65`
  - 컴퓨터에겐 영문 대문자 `A`
  - 16진수로는 `0x41`
- 숫자와 글자를 구별하지 않고 정보를 말할 때는 `바이너리(Binary)`라고 한다.

## 컴퓨터가 사진을 다루는 방법
### Pixel (화소)
- 모니터 상의 `점` 하나
- 화소 하나를 표현하는데 `8bit` `16bit` `24bit` `32bit` 등의 정보가 필요할 수 있다.
  - 기본적으로 `RGB`만해도 `24bit - R(8)G(8)B(8)`가 필요함. 
  - bit 수가 클수록 점 하나에 여러 정보가 들어가겠구나
  - `32bit True Color`는 `RGB 24bit` + `Alpha-channel 8bit(투명도)` = `RGBA`
  - 용량으로는 `32bit` = `4byte`

### bitmap 
  - 해상도가 `1024 * 768` 이라면 픽셀이 저만큼 있는거고, 1 픽셀당 4byte 이므로
  - 1024 * 768 * 4byte 가 크기가 된다.
  - 이러한 정보를 모두 가지고 있는 파일을 `bitmap` 파일이라고 한다.
  - 용량이 너무 크다 -> `jpg`, `png` ...
    - 색 정보를 뭉뚱그려서 압축하여 저장하는 방식들이 있는 것이다.

  [이미지압축관련-유튜브영상](https://youtu.be/tHvZngU14jE?si=8zcbZqwKGvqLe8Lb)

