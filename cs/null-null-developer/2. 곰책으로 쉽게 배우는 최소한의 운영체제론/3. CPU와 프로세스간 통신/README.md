# CPU와 프로세스간 통신

---

## 1. CPU 스케줄링 개요

- OS는 Process가 동작하는 것을 서포트 하는 역할이다.
- 각 Process들에 속한 Thread들이 컴퓨터 자원, 특히 `CPU`를 선점하여 연산을 처리하려고 할 것이다.
- 이 때, 어떤 Process에게 얼만큼 연산 기회를 줄 것인지를 OS가 관리하게 되며, 이를 `CPU 스케줄링`이라고 한다.
- CPU를 스케줄링 하는 가장 큰 목적은 `시스템 과부하 상태를 막는 것`이다.

    <img src="https://github.com/jiyongYoon/study_db_realmysql/assets/98104603/80cd34da-d5a2-44f2-a129-f886b1c22dd7" alt="adder" width="50%" />

  - Level 1, 고수준 스케줄링 - 전체 시스템의 부하를 고려하여 작업을 시작할지 말지를 결정한다. 멀티 테스킹 작업을 어느정도 가져갈지를 결정한다고 생각하면 좋다.
  - Level 2, 중간 수준 스케줄링 - 시스템에 과부하가 걸려서 전체 프로세스 수를 조절해야 한다면 이미 활성화된 프로세스 중 일부를 보류 상태로 보낸다.
  - Level 3, 저수준 스케줄링 - 실제 작업을 수행한다. 동작하는 작업에 대한 미세 조정을 한다. 

### 스케줄링 방식

- 선점형: 어떤 프로세스가 CPU를 선점하여 작업중이더라도 OS가 강제로 CPU를 빼앗아 다른 프로세스에게 전해줄 수 있는 스케줄링 방식이다.
- 비선점형: 어떤 프로세스가 CPU를 선점하여 작업을 시작하면, 그 프로세스가 작업이 끝날 때 까지 다른 프로세스가 기다려야만 하는 방식이다.
- **우선순위**
  - 5단계로 나누어 CPU 작업 할당 비중을 정해주는 것이다.
    - ex1) 미디어 플레이어를 실행시킬 때, 영상이 많이 끊긴다면 우선순위를 높여서 CPU 작업을 많이 하도록 자원을 밀어줄 수 있다.
      - 그렇다면 반대로 다른 프로세스들은 그만큼 연산을 못하게 될 것이다.
    - ex2) 대용량 파일을 옮기는 작업같은 경우는 우선순위를 낮춰서 백그라운드로 동작시켜 긴 시간 작업을 하는 동안 다른 프로세스의 작업을 원활하게 한다.
      - 대신 조금 더 오래 걸리긴 하겠다.

---

## 2. 프로세스간 통신 개요(IPC) (중요!)

<img src="https://github.com/jiyongYoon/study_db_realmysql/assets/98104603/c15e8c71-d7ff-402a-818c-8740770d8285" alt="adder" width="50%" />

1. RAM Shared Memory
   
    <img src="https://github.com/jiyongYoon/study_db_realmysql/assets/98104603/a610037a-89f0-4f73-971d-47ad6f9469de" alt="adder" width="40%" />

2. Pipe (File) - 프로세스 간 통신
3. Socket - 네트워크 수준
4. RPC(Remote Procedure Call)
5. Registry in Memory

