# CPU와 Process, Thread

---

## 1. CPU도 당신처럼 미리 예측하고 움직인다.

- CPU는 연산장치이므로 `속도`가 매우 중요하다.
- CPU는 연산을 위한 데이터를 `RAM`에서 가져오게 된다.
  - 기본적으로 CPU는 RAM보다 50배정도 빠르다.
  - 따라서 중간에 완충장치가 필요하다. => `Cache Memory`

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/b24ea7e8-b04a-4490-b3cd-6b0ee4aeeb50" alt="adder" width="70%" />

### 예측에 대한 문제는 없는건가?? -> [CPU게이트](https://namu.wiki/w/CPU%20%EA%B2%8C%EC%9D%B4%ED%8A%B8)

- 참조가 자주 일어나게 되면 Caching을 하게 된다.
- 보여주지 않더라도 캐싱이 되어있는 값을 이용하여 원래 값을 알아내는 원리로 해킹을 하는 내용이다.

---

> 근래에는 패러다임이 변하고 있음. <br>
> 컴퓨터에는 연산장치가 하나 더 있다. `GPU!!`<br>
> 연산 종류에 따라 `GPU`를 사용하는 것이 성능이 더 나을 수 있다!
> 
> 추가로, `PIM(Processing In Memory)`. 즉, 기억장치였던 RAM에서 `전처리`를 하는 것으로 변하고 있다!


---

## 2. 프로세스와 스레드

- [프로세스와 스레드의 차이 Youtube 영상](https://youtu.be/x-Lp-h_pf9Q?si=OoL1cHrTJpRjNM6Z)
 
<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/c12a27c8-1fec-4ea6-84c4-c63dbf6fd4ed" alt="adder" width="40%" />

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/7c7f7fbd-319f-4ed4-8589-4476e1de4588" alt="adder" width="70%" />

<br>

- Computer의 자원은 크게 `CPU`와 `Virtual Memory(RAN + HDD 일부)`이다.
- 프로세스 단위로 `Virtual Memory`가 주어진다.
  - 스레드들은 프로세스에 속하여 프로세스에 할당된 자원을 사용하게 된다.
  - 각 스레드들은 작업을 위해 본인이 속한 프로세스에 할당된 메모리 영역과, CPU를 사용하게 된다.
  - 우리가 요즘 보게 되는 CPU(8코어, 16코어 등등)는 코어 갯수가 한정적인데, 컴퓨터의 스레드는 몇천개가 멀티스레딩으로 돌아가고 있다. 이 많은 스레드가 본인들이 연산을 하기 위해 CPU를 사용하려고 하기 때문에, OS는 이러한 스레드들을 줄을 세워 관리하게 된다  <br>
  -> `시분할사용`(누구 잠깐 쓰고, 누구 잠깐 쓰고,,,) <br>
  -> 무척 짧은 시간이기 때문에 동시에 처리하는것처럼 느껴지게 된다.

### 프로세스

- (HDD에 설치된) 프로그램이 RAM 메모리에 올라가서 CPU의 연산이 진행되는 상태(Instance화)를 `Process`라고 한다.
- OS가 프로세스를 관리하는 자료구조는 `Queue`다.
  - 프로세스가 Ready 상태인 친구들이 줄을 서있다.
  - Dispatcher? 관리자, 관리원 -> Queue에 줄을 서서 실행할 Task들이 나열되어 있을 때, 앞에서부터 하나씩 꺼내서 실행한다. 

### 프로세스의 라이프 사이클

  <img src="https://github.com/jiyongYoon/study_db_realmysql/assets/98104603/236887e5-399d-437e-8095-b1c7d1568027" alt="adder" width="60%" />

  1. 생성 (new)
  2. 준비 (ready)
  3. 실행 (running) <br>
     iii-1. 대기 (waiting): Device에 I/O Request 요청을 하고 Response가 올 때까지 기다리는 상태
     - Blocking I/O: Response가 올 때까지 프로세스가 기다림 
     - Non-Blocking I/O: Response가 안와도 다른일 하러감(실행상태로 감)
  4. 완료 (terminated)

### 추가적인 프로세스의 상태

  5. 휴식 (Sleep): 자발적으로 이탈(ex, sleep() )
  6. 보류 (Suspend): 외부 요인으로 이탈(ex, OS 레벨에서 swap 날 때 ) 
  - 두 상태 모두 `Queue` 대기열에서 이탈하게 된다.
    - 일정 조건이 충족되면(ex, sleep(1000) -> 1초 후) 지난 후 대기열 맨 뒤로 다시 재진입한다.
  - 프로세스의 작업이 스위칭 될 때, 기존에 작업하다가 멈추었던 정보들을 저장해두었다가 다시 불러와야 한다. -> `PCB`

  <img src="https://github.com/jiyongYoon/study_db_realmysql/assets/98104603/d2474bc2-f0e7-4cce-827c-6c4658289db1" alt="adder" width="60%" />
  

- Windows 운영체제는 프로세스를 관리하기 위한 `PCB`(Process Control Block)와 스레드를 관리하기 위한 `TCB`가 있다.
  - PCB 안에는
    1) PID(양의 정수, 32bit)
    2) Memory 관련 정보: 작업할 때 필요한 데이터가 어디 있는지 알기 위해
    3) 프로그램 카운터(PC)
    4) 프로세스 우선순위
    5) 각종 레지스터 정보

### 프로세스의 생성과 복사

- 컴퓨터에서 프로그램이 실행되면 OS는 새로운 프로세스를 생성한다. `Windows: createProcess()` `UNIX: fork(), exec()`
  - [fork()와 exec()의 차이점](https://woochan-autobiography.tistory.com/207)
- 어떤 프로그램을 실행한다면 `현재 프로세스(부모 프로세스)`가 `새 프로세스(자식 프로세스)`를 생성한 것이다.
- 프로세스는 컴퓨터 자원(`CPU` + `가상메모리`)을 할당 받는다.
