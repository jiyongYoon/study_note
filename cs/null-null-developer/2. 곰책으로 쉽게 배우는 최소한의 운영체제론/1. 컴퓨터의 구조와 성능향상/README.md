# 컴퓨터의 구조와 성능향상

---

## 인터럽트란?

- '끼어들다', '중단시키다' 정도의 의미
- 발생하면 하던 일을 멈춘다!

## 종류

### 외부 인터럽트
- 전원 이상 인터럽트
- 기계 착오 인터럽트
- 외부 신호 인터럽트
- 입출력 인터럽트
  - 입출력장치가 1) 데이터 전송을 요구하거나, 2) 전송이 끝나 다음 동작이 수행되어야 할 경우

### 내부 인터럽트
- 잘못된 명령이나 잘못된 데이터를 사용할 때 발생하여 Trap이라고 부름
- 프로그램 검사 인터럽트
  - Division by Zero
  - Overflow / Underflow
  - 기타 Exception

## 동작 순서

1. 인터럽트 요청
2. 프로그램 실행 중단: 현재 실행중이던 Micro operation까지 수행한다.
3. 현재의 프로그램 상태 백업: PCB(Process Control Block), PC(Program Counter) 등
4. 인터럽트 서비스 루틴(ISR) 실행: 인터럽트 처리 코드가 동작한다.

---

## DirectX

- 기존 인터럽트를 사용한 H/W 동작에는 `API`를 통한 `System Call`만 가능했다.
1) `UserMode` Process의 `API(System Call)`
2) `KernelMode`의 엔진이 `Driver` 동작
3) `Driver`의 H/W 컨트롤 
- 그러나 고성능 작업이 필요한 곳에서는 성능이슈가 발생할 수 밖에 없었고(인터럽트가 많아지니 성능이 느려질 수 밖에), 이에 `System call`을 엔진을 통해서가 아니라 (엔진을 없애고) 직접 할 수 있도록 해준 것이다. <br> 
그게 바로 `DirectX`이다.

> 더 빠르게는 안될까? H/W의 메모리 레벨까지 한번에 접근할수는 없나? <br>
> => `DMA`(Direct Memory Access)

---

## DMA

- Direct Memory Access
- 원래는 CPU가 RAM을 사용하기 때문에 RAM 공간을 `KernelMode`에서 통제하지만, <br> 
DMA를 지원하는 장치라면 `KernelMode`에서 사용할 RAM 공간을 거치지 않고 `UserMode`의 램 공간을 다이렉트로 사용하기 때문에 각 단계에서 발생하는 I/O 버퍼가 사라지기 때문에 기능이 향상되게 되는 것이다.
- `가상화 환경`에서는 이 상황을 극대화할 수 있다
  - 한 컴퓨터 메모리 안에서 데이터가 돌아다니는 상황이기 때문에, 실제로는 RAM COPY만으로 모든 작업이 진행될 수 있다는 뜻.
