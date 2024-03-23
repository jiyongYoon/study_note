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