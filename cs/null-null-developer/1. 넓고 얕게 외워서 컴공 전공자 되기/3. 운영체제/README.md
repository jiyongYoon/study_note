# 운영체제

## 동시성과 병렬성

### 동시성
- 서로 다른 일이 동시에 일어날 가능성이 있는 것
  - ex) 라면을 먹으면서 TV를 보는 것. 동시에 할 수 있냐? YES! => 동시성이 있다.
  - 라면은 무조건 부엌에서 먹어야하고, TV는 무조건 거실에서 보아야하면? NO! => 동시성이 없다.

### 병렬성
- 같은 일을 여러 주체가 함께 동시에 진행할 가능성이 있는 것
  - ex) 인형 눈 붙이기를 여러명이서 작업하는 것

## 원자성과 동기화

### 원자성
- 한 자원을 한 작업자가 점유하는 것을 보장하는 것
  - ex) 화장실 한 칸을 사용하는 것 -> 화장실 칸: 자원, 마려운 사람: 작업자
  - 한 자원에 대한 이야기이기 때문에, `동시성의 가능성이 있을 때 원자성 보장을 논한다`

### 동기화
- 한 자원의 원자성 보장에 대한 교통정리를 하는 것

## 교착상태 (Deadlock)
- 서로 필요한 자원이 교차되어 작업이 멈춘 상황
  - ex) 휴지가 없어서 못 나가는 자 vs 나와야 들어가는 휴지 든 자

***


> 컴퓨터의 구성 요소
> - Computer = H/W + S/W
> - S/W = Application + System(`OS`)

## 프로그램, 프로세스, 스레드

### 프로그램
- 설치하는 것 (2차 기억장치에)

### 프로세스
- 프로그램을 실행한 것. `관리단위`
- 여러 프로세스가 동작하는 환경을 `멀티 테스킹` 환경이라고 한다.

### 스레드
- 프로세스 속에 존재하는 `실행단위`
- CPU를 사용하는 직접적인 주체.
- 스레드는 프로세스 내에 속해있다.
- 작업을 한다는 것은 `연산`을 한다는 것이고, `연산`을 위해서는 우리가 배웠던 `메모리`가 필요하다.
- 즉, 스레드는 프로세스에게 할당된 자원을 공유한다.

### Stack, Heap
- 메모리 공간의 두 종류

`Stack`
- 스레드가 사용한다.
- 즉, 스레드 `개인 자원`.

`Heap`
- 프로세스 전체가 사용한다.
- 즉, 프로세스 `공용 자원`.

### 왜 공간 구분을 해놓은 것일까?

- 동시성 처리 및 작업의 효율을 위해서 구분해놓은 것 

> `나`(로그인 하여 작업을 하는 주체) 는 컴퓨터 속에서 `프로세스`라는 모습으로 존재한다. <br>
> 대표적으로 `Shell`(윈도우 탐색기)

***

## 컴퓨터 세상의 3단계 구성요소

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/cc989398-817c-4733-ad34-5237eb9fc445" alt="adder" width="70%" />

- 크게는 H/W + S/W
- S/W = Application + System(`OS`)

### Application 
- 우리가 흔히 생각하는 `프로그램`들이 실행되는 공간.
- 여러 프로세스가 함께 실행되는 `멀티 테스킹` 환경이다.
- 각 프로세스는 `독립적`인 자원을 할당받으며, 서로의 공간에 접근하지 못한다.
  - 접근을 시도할 때 `OS`가 접근을 제어한다.

### System(OS)
- `kernel` 이라는 핵심 알멩이
  1. I/O 제어
  2. 자원 관리
  3. 접근 통제

### H/W
- CPU, Memory, 주변기기들이 존재함
- 해당 부분을 `S/W`화 한 것이 바로 `가상화` 기술의 핵심

### 동작 원리

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/6150b1a2-709d-4e54-98ee-900f1814e314" alt="adder" width="70%" />

**상황: Device를 동작시키려고 한다면?**
- H/W 레벨에서는 Device가 존재한다.
- H/W와 가장 가까운 S/W 레벨에서는 H/W를 통제할 수 있는 `Device Driver`가 존재한다.
- `Driver`에게 명령을 내릴 수 있는 핵심 기능을 가진 것을 `요소(Engine)`라고 한다.
- `Engine`에게 어떤 동작을 요청하기 위해서는 반드시 엔진이 제공하는 `Interface`를 통해야 한다.
  - 이 인터페이스를 `대상체`라고 부를 수 있으며, App 레벨에서 `File`의 형태로 존재한다.
  - `Kernel` 모드로 들어가는 이 부분의 interface를 정확한 용어로는 `System Call`이라고 부른다.
- 각 `Process`는 인터페이스(System Call)를 통해 Read / Write 작업이 이루어지게 된다.
  - `Driver`는 System Call을 받으면 `IRQ` (Interrupt Request)가 발생한다.
  - `IRQ`는 장치마다 고유 번호를 갖는다.

***

## 가상 메모리

- RAM 공간이 아니라 HDD 등의 2차 기억 장치의 공간을 1차 Memory로 사용하는 것.
- 어떤 프로세스가 메모리를 사용할 때, 해당 위치가 `RAM`일지 `가상 메모리`일지는 주소를 따라가봐야 알 수 있다. 교통정리는 `OS`가 효율적으로 관리한다.
  - `Page(보통 4KB 단위)`가 메모리를 점유하는 단위
  - A 프로세스가 `RAM`을 사용중인데 B 프로세스가 더 빠른 메모리가 필요한 상황이면, `OS`가 A 프로세스의 일정 페이지를 가상 메모리에 `Page-out` 할 수 있다.
- C++ 등에서 컨트롤하는 메모리는 `가상 메모리`

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/bc16cca1-29e5-4564-840a-b9b4225e54ec" alt="adder" width="70%" />

### 장점
- 각 프로세스 공간을 완벽하게 분리하고 통제할 수 있다.
- 프로세스 오류가 운영체제에 영향을 주지 못하도록 차단할 수 있다.
- RAM 이 부족해도 동작을 시킬 수 있는 메모리 공간을 확보해 동작시킬 수 있다.

### 단점(?)
- Page-in, Page-out 시 OS가 Process들을 잠시 멈추게 되는데, 그런 동작들이 빈번하게 발생하는 상황에서 동작 속도가 느려질 수 밖에 없다.

