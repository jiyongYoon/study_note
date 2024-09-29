# 10. HPA(Horizontal Pod Autoscaling)

---

<img src="https://github.com/user-attachments/assets/b63d2a45-ff63-43c6-932e-1156c0ba771a" alt="adder" width="100%" />

[Horizontal Pod Autoscaling](https://kubernetes.io/ko/docs/tasks/run-application/horizontal-pod-autoscale/)

- auto scaling 기준인 metric 목표에 맞추기 위해 목표물(Deployment 및 Deployment의 ReplicaSet)의 적정 크기를 주기적으로 조정함

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  namespace: anotherclass-123
  name: api-tester-1231-default
  labels:
    part-of: k8s-anotherclass
    component: backend-server
    name: api-tester
    instance: api-tester-1231
    version: 1.0.0
    managed-by: dashboard
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: api-tester-1231
  minReplicas: 2
  maxReplicas: 4
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 60
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 120
```

# 1. 필드 설명

## spec.scaleTargetRef

- `Deployment` Object 특정

## spec.minReplicas/maxReplicas

- 최소 / 최대 Replica 개수

## spec.metrics

- Scaling 기준

### type

- `Resource` - 리소스 메트릭 사용

### resource.target

- `averageUtilization` - 평균 사용량 기준.
    - 이 값이 60이라면 스케일링 대상에서 파드의 평균 사용률을 60% 까지는 기존 Pod 개수를 유지하게 된다.
    - 모든 컨테이너의 리소스 사용량이 합산되기 때문에 개별 컨테이너 리소스를 반영하지는 못한다.

## spec.behavior

- 잦은 스케일링을 방지하기 위한 기간 명시

### scaleUp

- `stabilizationWindowSeconds` - metric이 얼마간 유지되면 스케일 아웃 할 것인지
    - 이 값이 120이라면 120초 동안 metric 기준을 유지 시 스케일 아웃 진행

### scaleDown

- `stabilizationWindowSeconds` - metric이 얼마간 유지되면 스케일 인 할 것인지
    - 이 값이 600이라면 600초 동안 metric 기준을 하회할 경우 스케일 인이 진행
        - 잠깐 해소된 부하는 조금 더 시간을 지켜보고 스케일 인을 할 수 있도록 유도하게 됨

# 2. 실무에서의 HPA 사용방법

- 이론적으로는 `트래픽증가` → `CPU 사용량 증가` → `HPA 조건 충족 시 AutoScaleOut` → `부하 해소`와 같은 아름다운 그림이 그려지면 좋지만 실무에서는 트래픽이 갑자기 급증해서 서비스가 중단되는 경우가 더 많다…
- Pod가 많으면 평균 CPU 사용량이 천천히 증가하여 HPA 동작이 의미가 있다.
- 그러나 급작스러운 트래픽에는 장사가 없다. 따라서 HPA 정책으로 모든 스케일링을 기대하기 보다는 아래와 같은 대안들을 함께 마련하는 것이 중요하다.

## 1) 트래픽 분석 및 Pod 생성

- 서비스 특성에 따라 트래픽이 몰리는 시간이 존재할 가능성이 높다. 이 시간에 맞게 자원을 늘리고 줄이는 스케쥴링을 미리 해놓는 것이 훨씬 유용할 수 있다.

## 2) 대기열 아키텍쳐 구성

- 갑작스런 트래픽 증가로 인해 서비스가 중단되기보다는, 미리 우리 서비스와 서버의 처리 가능한 트래픽을 산정하고, 해당 트래픽이 넘는 경우 대기열을 통해 서비스 중단을 막는 것이 효과적이다.

# 3. 실습

## 1) HPA 수정 방법

### (1) HPA 수정

```bash
# k edit -n namespace hpa hpa-name
[root@k8s-master 1231]# kubectl edit -n anotherclass-123 hpa api-tester-1231-default
Edit cancelled, no changes made.
```

- vi 모드로 켜짐

### (2) HPA 삭제

```bash
# k delete -n namespace hpa hpa-name
[root@k8s-master 1231]# kubectl delete -n anotherclass-123 hpa api-tester-1231-default
```

### (3) HPA 등록

```bash
[root@k8s-master 1231]# kubectl apply -f - <<EOF
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  namespace: anotherclass-123
  name: api-tester-1231-default
  labels:
    part-of: k8s-anotherclass
    component: backend-server
    name: api-tester
    instance: api-tester-1231
    version: 1.0.0
    managed-by: dashboard
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: api-tester-1231
  minReplicas: 2
  maxReplicas: 4
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 60
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 120
EOF
```

## 2) 부하 확인

```bash
[root@k8s-master 1231]# kubectl get hpa -n anotherclass-123
NAME                      REFERENCE                    TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
api-tester-1231-default   Deployment/api-tester-1231   4%/60%    2         4         2          9h

[root@k8s-master 1231]# kubectl top -n anotherclass-123 pods
NAME                               CPU(cores)   MEMORY(bytes)
api-tester-1231-6f898b9fcc-2wbhg   5m           106Mi
api-tester-1231-6f898b9fcc-lbgjg   4m           110Mi

```

## 3) 에러

- Grafana로 kubernetes의 파드 cpu사용량과 scaling 정보를 보는 연습을 하고 있었는데, 오랫동안 화면이 나오지 않았다.

  <img src="https://github.com/user-attachments/assets/60ccea8f-ccd2-43e2-93ca-a95ff51fc44b" alt="adder" width="60%" />

  <img src="https://github.com/user-attachments/assets/080ee8f8-331b-4b2f-9d24-fa39c9e1567f" alt="adder" width="60%" />

  나오지 않던 저 부분들도 kubernetes가 동작 중이었는데 말이다.

- 원인을 확인해보니 Host Machine의 time sync의 문제였다.

    ```bash
    [root@k8s-master 1231]# timedatectl
                   Local time: Tue 2024-09-10 18:51:33 KST
               Universal time: Tue 2024-09-10 09:51:33 UTC
                     RTC time: Tue 2024-09-10 09:43:18
                    Time zone: Asia/Seoul (KST, +0900)
    System clock synchronized: no
                  NTP service: active
              RTC in local TZ: no
    ```

    - 이거 지난번에 만났던 문제였다… 싱크를 다시 맞춰주자

        ```bash
        [root@k8s-master 1231]# systemctl restart chronyd.service
        [root@k8s-master 1231]# timedatectl
                       Local time: Tue 2024-09-10 18:53:44 KST
                   Universal time: Tue 2024-09-10 09:53:44 UTC
                         RTC time: Tue 2024-09-10 09:45:19
                        Time zone: Asia/Seoul (KST, +0900)
        System clock synchronized: no
                      NTP service: active
                  RTC in local TZ: no
        
        [root@k8s-master 1231]# timedatectl
                       Local time: Fri 2024-09-13 23:59:36 KST
                   Universal time: Fri 2024-09-13 14:59:36 UTC
                         RTC time: Fri 2024-09-13 14:59:38
                        Time zone: Asia/Seoul (KST, +0900)
        System clock synchronized: yes
                      NTP service: active
                  RTC in local TZ: no
        ```

      위에처럼 바로 확인했을때는 sync가 안맞았는데, 한참 지나고나니 싱크가 맞춰졌다..

      잘 안되는 것 같아서 다른 방법으로 패키지를 깔던 도중 확인이 되었기 때문이다…

        ```bash
        [root@k8s-master 1231]# set-ntp true
        -bash: set-ntp: command not found
        [root@k8s-master 1231]# systemctl restart chronyd.service
        [root@k8s-master 1231]# timedatectl
                       Local time: Tue 2024-09-10 18:53:44 KST
                   Universal time: Tue 2024-09-10 09:53:44 UTC
                         RTC time: Tue 2024-09-10 09:45:19
                        Time zone: Asia/Seoul (KST, +0900)
        System clock synchronized: no
                      NTP service: active
                  RTC in local TZ: no
        [root@k8s-master 1231]# sudo yum install ntp
        Rocky Linux 8 - AppStream                                                                           5.7 kB/s | 4.8 kB     00:00
        Rocky Linux 8 - AppStream                                                                            11 MB/s |  12 MB     00:01
        Rocky Linux 8 - BaseOS                                                                              6.4 kB/s | 4.3 kB     00:00
        Rocky Linux 8 - BaseOS                                                                              7.7 MB/s | 6.1 MB     00:00
        Rocky Linux 8 - Extras                                                                              4.7 kB/s | 3.1 kB     00:00
        Rocky Linux 8 - Extras                                                                               23 kB/s |  14 kB     00:00
        Docker CE Stable - x86_64                                                                            62 kB/s | 3.5 kB     00:00
        Kubernetes                                                                                          3.3 kB/s | 1.7 kB     00:00
        No match for argument: ntp
        Error: Unable to find a match: ntp
        [root@k8s-master 1231]# set-ntp true
        -bash: set-ntp: command not found
        [root@k8s-master 1231]# ^C
        [root@k8s-master 1231]# timedatectl
                       Local time: Fri 2024-09-13 23:59:36 KST
                   Universal time: Fri 2024-09-13 14:59:36 UTC
                         RTC time: Fri 2024-09-13 14:59:38
                        Time zone: Asia/Seoul (KST, +0900)
        System clock synchronized: yes
                      NTP service: active
                  RTC in local TZ: no
        ```

      뭐 일단 싱크가 맞았으니, restart 명령어가 먹어서 이제 다시 뜬걸로…
