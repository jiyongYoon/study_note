# Thread

## Thread 디버깅

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/3f1bf785-739d-40bb-9a12-f9b8010f61f9" alt="adder" width="60%" />

- 디버거의 Threads 탭에 가면, 현재 main 스레드를 포함한 총 6개의 스레드가 동작하고 있다는 것을 알수 있다.

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/b8428a33-a364-47b1-b4bf-71592ab8bf2e" alt="adder" width="60%" />

- `thread1.start()` 메서드를 지나니 HelloThread라는 스레드가 동작 중이라는 것을 알 수 있다.

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/2022e7af-771b-4d3a-9491-ceb27bb67fe8" alt="adder" width="60%" />

- Thread.sleep까지 마치니 동작이 모두 끝난 HelloThread는 JVM이 파기한 것을 알 수 있다.

<img src="https://github.com/jiyongYoon/study_note/assets/98104603/7031a412-a4d5-44ed-8442-5592f753648a" alt="adder" width="60%" />

- Thread 내에서 Exception을 직접 처리할수도 있다.

## Thread의 종료

### Thread를 종료해야하는 이유

1. 아무것도 하지 않고있어도, 스레드가 살아있다면 리소스를 잡아먹는다. (메모리와 커널, 만약 동작한다면 CPU까지 점유)
2. 예기치 못하게 잘못된 동작을 계속 반복하거나, 너무 긴 작업을 하고 있거나 등, 개발자의 의도에 벗어나는 동작을 하는 경우
3. 프로그램을 종료하고 싶은 경우 (스레드가 한 개라도 작업중이라면 당장 프로세스가 종료되지 못한다)

### Thread를 종료하는 방법

`thread.interrupt()`

- A 스레드에서 B 스레드의 interrupt() 메서드를 호출하면, B 스레드에 interrupt가 발생한다.
  - 이 때, 두가지 상황이 생긴다.
    1. B 스레드가 `Thread.sleep()` 등의 상황에 멈취있는 등 `try - catch` 블럭에서 `InterruptedException`을 처리하는 경우
       - 이 경우에는 interrupt 발생 시 `InterruptedException` 가 발생하여 catch 블럭에서 핸들링한 대로 처리된다.
    2. B 스레드가 정상적인 작업중인 경우
       - 이 경우에는 interrupt가 발생한다고 해당 스레드가 영향을 받지는 않는다. 따라서 예시 코드와 같이 긴 작업을 하는 경우, 
       interrupt가 발생하면 멈출 수 있도록 처리를 해줄 수 있다.

       <img src="https://github.com/jiyongYoon/study_note/assets/98104603/737f5d23-9b13-4d03-ae2e-fe26037a469e" alt="adder" width="60%" />
