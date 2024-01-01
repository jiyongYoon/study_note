# 컴퓨터 구조

## CPU
- 전자식 계산기
- 사칙연산 등을 하게 되는데, 덧셈(가산)을 먼저 생각해보자

### 덧셈
- 반가산기: 두 bit를 덧셈하는 가산기

  <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/12ff5ee5-0ed4-4a04-a0d7-2a64ef981044" alt="half-adder" width="40%" />
  
    - 두 이진수(1bit + 1bit) 의 합(C는 자리올림)을 계산할 수 있음.
    - 그렇다면 자리수가 여러개 올라갈 수 있는 8bit 수는 어떻게 더하나?
- 전가산기: 두 bit 외에 자리올림(carry)까지 계산할 수 있음

    <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/a55f69d2-f3c6-466f-80a8-330e8421bd18" alt="adder" width="40%" />
  
    - 전가산기 n개를 조합하면 n-bit 계산이 가능해짐

### 뺄셈은?
- 보수를 더하면 뺄셈이 된다. 그래서 덧셈으로 뺄셈처리도 가능해진다.

### 곱셈은?
- 덧셈 여러번. 그러나 비효율적 -> `Shift`
- 2진수를 왼쪽으로 옆으로 한번 밀면 `X2`가 된다.
  - 예시) 10진수 5 = 0101<sub>(2)</sub> --`Shift`--> 1010<sub>(2)</sub> = 10진수 10
  - 단, 자리올림은 버리고 새로 올라오는 자리는 `0`으로 채운다. (Zero Padding)

### 나눗셈은?
- 뺄셈 여러번. 그러나 비효율적 -> `Shift`
- 곱셈과 반대로 오른쪽으로 밀면 된다.
  - 예시) 10진수 6 = 0110<sub>(2)</sub> --`Shift`--> 0011<sub>(2)</sub> = 10진수 3
  - 단, 자리내림은 버리고 새로 생기는 자리는 `0`으로 채운다. (Zero Padding)

> 연산은 다했다. 그래서?

## Memory 

### RAM

- 1차 저장장치
- 일련번호(주소) : 값(정보) 로 구성된다
- CPU는 필요한 정보를 Memory로부터 제공받고, 제공받은 정보는 CPU의 `Register`에 잠깐 담아놓고 사용한다.

### Magnetic Disk

- 2차 저장장치
- Track & Sector 구조로 관리된다
- Sector는 보통 `512bytes` 단위로 배정된다
- 사용기간동안 같은 Sector에 Write가 많이 발생하게 되면(대략 10만번) 더이상 해당 Sector는 기능을 상실하게 되며, 그런 부분을 `Bad Sector`라고 한다
- 예전에 `디스크 조각모음`을 했던게, 같은 파일의 여러 Track 및 Sector에 나누어져있는 것을 연속된 곳에 모아주는 작업을 하던 것이다. 그래서 I/O 작업 효율을 높이는 것이다
- 0번 Track 0번 Sector는 MBR(Master Boot Record) -> OS 부트 관련 정보

### CPU ~ Memory 단계 구조

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/6e1f8b0e-c7c5-4f5e-a1a6-4c5a050eabd7" alt="cpu-ram" width="80%" />


### 파일시스템

- `FAT`(File Allocation Table) : 몇번 트랙 몇번 섹터에 어떤 파일이 있는지 기록해둔 Table
  - 파일을 삭제한다고 저장장치에서 해당 데이터를 지우는 등의 Overwrite 작업을 하는 것이 아니다. table에 파일명을 좀 지우고 지워진 파일이라고 마킹만 하는 것.
  - `HDD 복원작업`이 `FAT`을 분석해서 섹터 데이터가 살아있으면 삭제 마킹을 지우고 파일명을 복원하는 작업을 하는 것이다
- `Format 작업`은 파일관련 메타데이터 테이블 관련 작업인 것이다
  - 빠른 포멧은 테이블만 날리는것
  - 느린 포멧은 테이블도 남기고 Sector도 다 Overwrite 해버리는것
- Windows의 파일시스템은 `NTFS`
- `SSD`는 디스크가 아니라 칩으로 바뀐것 뿐, 관리 측면에서는 동일함