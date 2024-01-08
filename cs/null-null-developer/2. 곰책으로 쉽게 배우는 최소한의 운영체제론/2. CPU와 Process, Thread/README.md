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

