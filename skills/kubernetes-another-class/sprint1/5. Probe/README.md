# 5. Probe

---

# 1. Probe란

- 애플리케이션을 편하게 관리하기 위한 기능으로, 앱의 상태를 진단하는 기능을 가지고 있다.

# 2. Probe의 종류

- startupProbe
- readinessProbe
- livenessProbe

## yaml 파일과 기본 필드

```yaml
startupProbe:
	httpGet:
		path: "/startup"
		port: 8080
	periodSeconds: 5
	successThreshold: 1
	failureThreshold: 24
readinessProbe:
	httpGet:
		path: "/readiness"
		port: 8080
	periodSeconds: 10
	successThreshold: 1
	failureThreshold: 3
livenessProbe:
	httpGet:
		path: "/liveness"
		port: 8080
	periodSeconds: 10
	successThreshold: 1
	failureThreshold: 3
```

- httpGet: http 통신 세팅
    - path
    - port
- exec: 실행 세팅
    - command: 명령어 세팅

        ```yaml
        // 예시
        readinessProbe:
        	exec:
        		command: ["cat", "/usr/src/myapp/datasource/postgresql-info.yaml"]
        ```

- periodSeconds:  동작 기간 (텀)
- successThreshold: 성공 판단 기준 횟수
- failureThreshold: 실패 판단 기준

## startupProbe

- 앱 기동을 체크하는 Probe
- `Deployment` 오브젝트로 Pod가 생성되면 그 때부터 실행됨
- `successThreshold` 갯수만큼 성공하면 앱 기동이 성공했다고 판단하며, **이 후 readinessProbe 와 livenessProbe 를 활성화시킴**
- `failureThreshold`갯수만큼 실패하면 앱 기동 실패로 판단함

## livenessProbe

- 앱이 기동을 잘 유지하는지 체크하는 Probe
- startupProbe의 동작이 성공하면 파드가 실행되는 동안 지속적으로 동작함
- `successThreshold` 갯수만큼 성공하면 앱이 제대로 기동되고 있다고 판단함
- `failureThreshold` 갯수만큼 실패하면 앱이 제대로 기동되지 않고 있다고 판단하며, **이 후 새로운 Pod를 실행시켜 앱을 다시 재기동시킴**

## readinessProbe

- 앱이 외부의 트래픽을 받을 준비가 됐는지 체크하는 Probe
- startupProbe의 동작이 성공하면 파드가 실행되는 동안 지속적으로 동작함
- `successThreshold` 갯수만큼 성공하면 `Service` 오브젝트를 통해 Pod에 외부 트래픽이 들어올 수 있도록 연결을 함
- `failureThreshold` 갯수만큼 실패하면 앱이 제대로 기동되지 않고 있다고 판단하며, **이 후 외부 트래픽을 차단시킴**

## livenessProbe와 readinessProbe는 무슨 차이?

- 두 Probe는 앱이 기동되는 동안 Health Check를 하는 Probe처럼 보이지만, UnHealth 시 후처리 방법이 다름
- livenessProbe는 파드를 재기동시키기 위한 진단 Probe이며
- readinessProbe는 외부 트래픽을 차단시키기 위한 진단 Probe이다.

# 3. App의 라이프사이클과 Probe의 동작

<img src="https://github.com/user-attachments/assets/816f2228-f00c-48ee-a599-cb9e49d4b205" alt="adder" width="100%" />

## livenessProbe와 readinessProbe의 활용

- 일시적 장애 상황에서(요청 과부하, 메모리 Leak, 쓰레드 Full 등) 자연 해소 시간을 확보하기 위해서는 `livenessProbe`의 주기를 조금 더 길게 가져가서 Pod가 쉽게 재기동 되는 것을 방지하는 것도 한 가지 방법이 될 수 있다.
- 그리고 이런 상황에서 `readinessProbe`는 서버의 자연 해소 시간을 확보해주기 위해 외부 트래픽을 잠시 차단시키는 역할을 해줄 수 있다.
- 때문에 필요에 따라, 서버의 상태와 정책에 따라 어떻게 세팅하고 사용할지 탄력적으로 정할 수 있음.
- 예시
    - 일시적 장애 상황이 시작된 후, 30초 후에는 외부 트래픽 차단, 3분 뒤에는 App이 재기동 되도록 설정하기

        ```yaml
        startupProbe:
          httpGet:
            path: "/startup"
            port: 8080
          periodSeconds: 5
          failureThreshold: 10
        readinessProbe:
          httpGet:
            path: "/readiness"
            port: 8080
          periodSeconds: 10
          failureThreshold: 3
        livenessProbe:
          httpGet:
            path: "/liveness"
            port: 8080
          periodSeconds: 60
          failureThreshold: 3
        ```


## 실패 로그

- Probe의 실패 동작 로그는 대시보드의 파드 상세정보의 이벤트 탭에서 확인이 가능하다.

---
### 참고할 자료
[kubernetes Pod의 진단을 담당하는 서비스 : probe](https://medium.com/finda-tech/kubernetes-pod의-진단을-담당하는-서비스-probe-7872cec9e568)