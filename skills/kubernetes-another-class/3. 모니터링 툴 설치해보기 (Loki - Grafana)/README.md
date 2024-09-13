# 3. 모니터링 툴 설치해보기 (Loki - Grafana)

---

# 1) 모니터링 체험 중 에러 발생

일단 강의에서 안내하는대로 Loki, Grafana를 설치해서 Pod를 띄워보았다.
근데 한참을 기다려도 아래와 같은 상태만 유지되었다.

```bash
[root@k8s-master monitoring]# kubectl get pods -n loki-stack
NAME                        READY   STATUS              RESTARTS   AGE
loki-stack-0                0/1     ContainerCreating   0          7m35s
loki-stack-promtail-8284p   0/1     ContainerCreating   0          7m35s

[root@k8s-master monitoring]# kubectl get pods -n monitoring
NAME                                   READY   STATUS              RESTARTS   AGE
grafana-646b5d5dd8-l4vzw               0/1     ContainerCreating   0          7m54s
kube-state-metrics-86c66b4fcd-b572b    0/3     ContainerCreating   0          7m54s
node-exporter-cbtzt                    2/2     Running             0          7m54s
prometheus-adapter-648959cd84-4xpvs    0/1     ContainerCreating   0          7m53s
prometheus-operator-7ff88bdb95-m77zb   0/2     ContainerCreating   0          7m53s

```

컨테이너가 생성중에 멈춘 듯 하다.
기존에 깔아두었던 대시보드에서 pod의 상태를 봐야겠다고 생각했다.
다 동일한 상태로 멈춰있는 것으로 보인다.

> Failed to create pod sandbox: rpc error: code = Unknown desc = failed to setup network for sandbox "cb1bbff4152027799db3a9479ff9c3e874cdcfcd570a8758e8692c0617eb0aa6": plugin type="calico" failed (add): error getting ClusterInformation: connection is unauthorized: Unauthorized
>

역시나 쉽게 될리가 없다…

검색을 해보니 Calico 파드를 재실행하면 문제가 해결된다고 한다.

[[Solved] ClusterInformation: connection is unauthorized: Unauthorized – Failed to create pod sandbox: rpc error](https://devopscube.com/clusterinformation-connection-unauthorized/)

일단 Calico 파드는 잘 떠있는지 확인해보자

```bash
[root@k8s-master monitoring]# kubectl get -n calico-system pod
NAME                                       READY   STATUS    RESTARTS   AGE
calico-kube-controllers-658fd9c668-x9qqm   1/1     Running   0          2d1h
calico-node-6jmhk                          1/1     Running   0          2d1h
calico-typha-6b89c57df4-pzt8n              1/1     Running   0          2d1h
csi-node-driver-zfhcd                      2/2     Running   0          2d1h

[root@k8s-master monitoring]# kubectl get -n calico-apiserver pod
NAME                               READY   STATUS    RESTARTS   AGE
calico-apiserver-97757855f-p6csv   1/1     Running   0          2d1h
calico-apiserver-97757855f-pk4zn   1/1     Running   0          2d1h
```

다들 잘 떠있는 것으로 보인다…
그럼 이제, 파드를 어떻게 재실행하지?
일단 동작중인 파드를 먼저 확인해보자

```bash
[root@k8s-master monitoring]# kubectl get pod
No resources found in default namespace.
```

왜 날 괴롭히는가.

[Kubernetes Deployments No resources](https://enumclass.tistory.com/125)

글을 읽어보니, namespace를 명시해주어야 해당 namespace의 pod들을 볼 수 있다고 한다. 기본값이 default 인데, 그 namespace에는 아무 pod가 없어서 안뜨는거라고 한다.
모든 namespace를 볼 수 있는 명령어를 얻었다.

```bash
[root@k8s-master monitoring]# kubectl get pod --all-namespaces
NAMESPACE              NAME                                         READY   STATUS              RESTARTS     AGE
calico-apiserver       calico-apiserver-97757855f-p6csv             1/1     Running             0            2d1h
calico-apiserver       calico-apiserver-97757855f-pk4zn             1/1     Running             0            2d1h
calico-system          calico-kube-controllers-658fd9c668-x9qqm     1/1     Running             0            2d2h
calico-system          calico-node-6jmhk                            1/1     Running             0            2d2h
calico-system          calico-typha-6b89c57df4-pzt8n                1/1     Running             0            2d2h
calico-system          csi-node-driver-zfhcd                        2/2     Running             0            2d2h
kube-system            coredns-5d78c9869d-7vsvz                     1/1     Running             0            2d2h
kube-system            coredns-5d78c9869d-dmp4m                     1/1     Running             0            2d2h
kube-system            etcd-k8s-master                              1/1     Running             0            2d2h
kube-system            kube-apiserver-k8s-master                    1/1     Running             0            2d2h
kube-system            kube-controller-manager-k8s-master           1/1     Running             1 (2d ago)   2d2h
kube-system            kube-proxy-lw5lc                             1/1     Running             0            2d2h
kube-system            kube-scheduler-k8s-master                    1/1     Running             1 (2d ago)   2d2h
kube-system            metrics-server-7db4fb59f9-4nrss              1/1     Running             1 (2d ago)   2d2h
kubernetes-dashboard   dashboard-metrics-scraper-5cb4f4bb9c-kdbpj   1/1     Running             0            2d2h
kubernetes-dashboard   kubernetes-dashboard-6bc7c98694-7rp9k        1/1     Running             1 (2d ago)   2d2h
loki-stack             loki-stack-0                                 0/1     ContainerCreating   0            28m
loki-stack             loki-stack-promtail-8284p                    0/1     ContainerCreating   0            28m
monitoring             grafana-646b5d5dd8-l4vzw                     0/1     ContainerCreating   0            28m
monitoring             kube-state-metrics-86c66b4fcd-b572b          0/3     ContainerCreating   0            28m
monitoring             node-exporter-cbtzt                          2/2     Running             0            28m
monitoring             prometheus-adapter-648959cd84-4xpvs          0/1     ContainerCreating   0            28m
monitoring             prometheus-operator-7ff88bdb95-m77zb         0/2     ContainerCreating   0            28m
tigera-operator        tigera-operator-84cf9b6dbb-fvd44             1/1     Running             1 (2d ago)   2d2h

```

굳.. namespace가 위와 같았구나

```bash
kubectl get <resource> -n <namespace>
```

```bash
[root@k8s-master monitoring]# kubectl get pod -n calico-system
NAME                                       READY   STATUS    RESTARTS   AGE
calico-kube-controllers-658fd9c668-x9qqm   1/1     Running   0          2d2h
calico-node-6jmhk                          1/1     Running   0          2d2h
calico-typha-6b89c57df4-pzt8n              1/1     Running   0          2d2h
csi-node-driver-zfhcd                      2/2     Running   0          2d2h
```

근데 이게 하나씩 껐다 키는게 아니라 뭉터기로 껐다 키는게 있을 것 같다. (마치 docker-compose처럼..)

[[k8s] pod restart #pod 재시작 하기](https://hello-bryan.tistory.com/259)

deployment라는 개념을 새로 보게 되었다. 아마도 pod 묶음을 실행시키는 묶음 단위로 보인다. (추정)

```bash
[root@k8s-master monitoring]# kubectl get deployments
No resources found in default namespace.
```

아 이거 알지

```bash
[root@k8s-master monitoring]# kubectl get deployments --all-namespaces
NAMESPACE              NAME                        READY   UP-TO-DATE   AVAILABLE   AGE
calico-apiserver       calico-apiserver            2/2     2            2           2d2h
calico-system          calico-kube-controllers     1/1     1            1           2d2h
calico-system          calico-typha                1/1     1            1           2d2h
kube-system            coredns                     2/2     2            2           2d2h
kube-system            metrics-server              1/1     1            1           2d2h
kubernetes-dashboard   dashboard-metrics-scraper   1/1     1            1           2d2h
kubernetes-dashboard   kubernetes-dashboard        1/1     1            1           2d2h
monitoring             grafana                     0/1     1            0           39m
monitoring             kube-state-metrics          0/1     1            0           39m
monitoring             prometheus-adapter          0/1     1            0           39m
monitoring             prometheus-operator         0/1     1            0           39m
tigera-operator        tigera-operator             1/1     1            1           2d2h

```

오케이 나는 calico와 관련된 애들을 재시작 하고 싶다.
블로그에서 제안한 방법으로 해보자.

```bash
[root@k8s-master monitoring]# kubectl scale deployment calico-kube-controllers --replicas=0
error: no objects passed to scale
```

안먹는다.

```bash
[root@k8s-master monitoring]# kubectl rollout restart deployment calico-kube-controllers
Error from server (NotFound): deployments.apps "calico-kube-controllers" not found
```

deployments.apps에 그게 없다네… 음… 저게 namespace이려나..?
이상해서 deployment라는 리소스에 이름으로 검색을 다시 해보았다.

```bash
[root@k8s-master monitoring]# kubectl get deployment -n calico-system
NAME                      READY   UP-TO-DATE   AVAILABLE   AGE
calico-kube-controllers   1/1     1            1           2d2h
calico-typha              1/1     1            1           2d2h
```

잘 있는데…. 혹시 명령어가 잘못되었나…?

```bash
[root@k8s-master monitoring]# kubectl rollout restart deployment -n calico-system
deployment.apps/calico-kube-controllers restarted
deployment.apps/calico-typha restarted
```

하하… 이름은 항상 -n 옵션을 주어야하나보다..

[kubectl rollout restart](https://kubernetes.io/docs/reference/kubectl/generated/kubectl_rollout/kubectl_rollout_restart/)

역시 공식문서를 보는게 필요해보인…
calico-apiserver도 rollout restart 해주었다.
다시 잘 떴나 볼까?

```bash
[root@k8s-master monitoring]# kubectl get deployment --all-namespaces
NAMESPACE              NAME                        READY   UP-TO-DATE   AVAILABLE   AGE
calico-apiserver       calico-apiserver            0/2     0            0           2d2h
calico-system          calico-kube-controllers     0/1     0            0           2d2h
calico-system          calico-typha                1/1     1            1           2d2h
kube-system            coredns                     2/2     2            2           2d2h
kube-system            metrics-server              1/1     1            1           2d2h
kubernetes-dashboard   dashboard-metrics-scraper   1/1     1            1           2d2h
kubernetes-dashboard   kubernetes-dashboard        1/1     1            1           2d2h
monitoring             grafana                     0/1     1            0           66m
monitoring             kube-state-metrics          0/1     1            0           66m
monitoring             prometheus-adapter          0/1     1            0           66m
monitoring             prometheus-operator         0/1     1            0           66m
tigera-operator        tigera-operator             1/1     1            1           2d2h
```

왜 다시 안뜨지…?

```bash
[root@k8s-master monitoring]# kubectl get pods --all-namespaces
NAMESPACE              NAME                                         READY   STATUS              RESTARTS       AGE
calico-apiserver       calico-apiserver-97757855f-p6csv             0/1     Terminating         0              2d2h
calico-apiserver       calico-apiserver-97757855f-pk4zn             0/1     Terminating         0              2d2h
calico-system          calico-kube-controllers-658fd9c668-x9qqm     0/1     Terminating         0              2d2h
calico-system          calico-node-6jmhk                            1/1     Running             0              2d2h
calico-system          calico-typha-6df968957f-sksjk                1/1     Running             0              5m53s
calico-system          csi-node-driver-zfhcd                        2/2     Running             0              2d2h
kube-system            coredns-5d78c9869d-7vsvz                     1/1     Running             0              2d2h
kube-system            coredns-5d78c9869d-dmp4m                     1/1     Running             0              2d2h
kube-system            etcd-k8s-master                              1/1     Running             0              2d2h
kube-system            kube-apiserver-k8s-master                    1/1     Running             0              2d2h
kube-system            kube-controller-manager-k8s-master           1/1     Running             1 (2d1h ago)   2d2h
kube-system            kube-proxy-lw5lc                             1/1     Running             0              2d2h
kube-system            kube-scheduler-k8s-master                    1/1     Running             1 (2d1h ago)   2d2h
kube-system            metrics-server-7db4fb59f9-4nrss              1/1     Running             1 (2d1h ago)   2d2h
kubernetes-dashboard   dashboard-metrics-scraper-5cb4f4bb9c-kdbpj   1/1     Running             0              2d2h
kubernetes-dashboard   kubernetes-dashboard-6bc7c98694-7rp9k        1/1     Running             1 (2d1h ago)   2d2h
loki-stack             loki-stack-0                                 0/1     ContainerCreating   0              67m
loki-stack             loki-stack-promtail-8284p                    0/1     ContainerCreating   0              67m
monitoring             grafana-646b5d5dd8-l4vzw                     0/1     ContainerCreating   0              67m
monitoring             kube-state-metrics-86c66b4fcd-b572b          0/3     ContainerCreating   0              67m
monitoring             node-exporter-cbtzt                          2/2     Running             0              67m
monitoring             prometheus-adapter-648959cd84-4xpvs          0/1     ContainerCreating   0              67m
monitoring             prometheus-operator-7ff88bdb95-m77zb         0/2     ContainerCreating   0              67m
tigera-operator        tigera-operator-84cf9b6dbb-fvd44             1/1     Running             1 (2d1h ago)   2d2h
```

걍 종료가 됐네…
뭐가 잘 안된다…. 재부팅해보자…

```bash
[root@k8s-master ~]# k get pods -A
NAMESPACE              NAME                                         READY   STATUS             RESTARTS       AGE
calico-apiserver       calico-apiserver-55cf88b6d5-9n8jk            1/1     Running            2 (4m7s ago)   7m53s
calico-apiserver       calico-apiserver-55cf88b6d5-kj6ww            1/1     Running            2 (4m7s ago)   7m53s
calico-system          calico-kube-controllers-667bb7485f-rdh65     1/1     Running            2 (4m7s ago)   7m52s
calico-system          calico-node-6jmhk                            1/1     Running            3 (4m6s ago)   3d1h
calico-system          calico-typha-6df968957f-sksjk                1/1     Running            6 (3m6s ago)   22h
calico-system          csi-node-driver-zfhcd                        2/2     Running            6 (4m7s ago)   3d1h
kube-system            coredns-5d78c9869d-7vsvz                     1/1     Running            3 (4m7s ago)   3d1h
kube-system            coredns-5d78c9869d-dmp4m                     1/1     Running            3 (4m7s ago)   3d1h
kube-system            etcd-k8s-master                              1/1     Running            3 (4m6s ago)   3d1h
kube-system            kube-apiserver-k8s-master                    1/1     Running            3 (4m7s ago)   3d1h
kube-system            kube-controller-manager-k8s-master           1/1     Running            4 (4m6s ago)   3d1h
kube-system            kube-proxy-lw5lc                             1/1     Running            3 (4m7s ago)   3d1h
kube-system            kube-scheduler-k8s-master                    1/1     Running            4 (4m7s ago)   3d1h
kube-system            metrics-server-7db4fb59f9-4nrss              1/1     Running            4 (4m7s ago)   3d1h
kubernetes-dashboard   dashboard-metrics-scraper-5cb4f4bb9c-kdbpj   1/1     Running            3 (4m7s ago)   3d1h
kubernetes-dashboard   kubernetes-dashboard-6bc7c98694-7rp9k        1/1     Running            4 (4m7s ago)   3d1h
loki-stack             loki-stack-0                                 1/1     Running            1 (4m7s ago)   23h
loki-stack             loki-stack-promtail-8284p                    0/1     CrashLoopBackOff   7 (79s ago)    23h
monitoring             grafana-646b5d5dd8-l4vzw                     0/1     CrashLoopBackOff   7 (9s ago)     23h
monitoring             kube-state-metrics-86c66b4fcd-b572b          2/3     CrashLoopBackOff   10 (62s ago)   23h
monitoring             node-exporter-cbtzt                          2/2     Running            6 (4m7s ago)   23h
monitoring             prometheus-adapter-648959cd84-4xpvs          0/1     CrashLoopBackOff   6 (89s ago)    23h
monitoring             prometheus-operator-7ff88bdb95-m77zb         1/2     CrashLoopBackOff   8 (74s ago)    23h
tigera-operator        tigera-operator-84cf9b6dbb-fvd44             1/1     Running            7 (3m ago)     3d1h
```

CrashLoopBackOff?

[누가 Kubernetes 클러스터에 있는 나의 사랑스러운 Prometheus 컨테이너를 죽였나!](https://engineering.linecorp.com/ko/blog/prometheus-container-kubernetes-cluster)

`CrashLoopBackOff`는 파드가 비정상 종료와 재시작을 반복하는 상태

…

여기까지 하다가 해결에 시간이 너무 오래걸려서 일단은 pass 하기로 결심했다.

## 해결

Virtual Box에 VM을 아예 새로 깔아보았더니 문제가 해결되었다.
맨 처음 설치할 때 calico 서비스가 잘못 되었었나 보다. 분명히 설치하면서 로그를 다 확인했었는데… 억울하긴 하지만…

# 2) 쿠버네티스의 기능 체험

## 테스트 할 Springboot 애플리케이션 pod 띄우기

- 전체 yaml 파일

    ```yaml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: app-1-2-2-1
    spec:
      selector:
        matchLabels:
          app: '1.2.2.1'
      replicas: 2
      strategy:
        type: RollingUpdate
      template:
        metadata:
          labels:
            app: '1.2.2.1'
        spec:
          containers:
            - name: app-1-2-2-1
              image: 1pro/app
              imagePullPolicy: Always
              ports:
                - name: http
                  containerPort: 8080
              startupProbe:
                httpGet:
                  path: "/ready"
                  port: http
                failureThreshold: 20
              livenessProbe:
                httpGet:
                  path: "/ready"
                  port: http
              readinessProbe:
                httpGet:
                  path: "/ready"
                  port: http
              resources:
                requests:
                  memory: "100Mi"
                  cpu: "100m"
                limits:
                  memory: "200Mi"
                  cpu: "200m"
    ---
    apiVersion: v1
    kind: Service
    metadata:
      name: app-1-2-2-1
    spec:
      selector:
        app: '1.2.2.1'
      ports:
        - port: 8080
          targetPort: 8080
          nodePort: 31221
      type: NodePort
    ---
    apiVersion: autoscaling/v2
    kind: HorizontalPodAutoscaler
    metadata:
      name: app-1-2-2-1
    spec:
      scaleTargetRef:
        apiVersion: apps/v1
        kind: Deployment
        name: app-1-2-2-1
      minReplicas: 2
      maxReplicas: 4
      metrics:
        - type: Resource
          resource:
            name: cpu
            target:
              type: Utilization
              averageUtilization: 40
    ```

    - 발생에러
        - dashboard에 들어가서 + 버튼으로 파드를 띄우려고 하는데, 아래와 같은 에러가 났다

          <img src="https://github.com/user-attachments/assets/c041513f-b1b6-4892-a875-c2f648135da6" alt="adder" width="50%" />

            - 해결
                - 왼쪽 상단에 모든 Namespace를 특정하고 작업을 해야하더라. (어떤 Namespace로 작업을 해야할지 몰라서 실패가 되는 것으로 보임)

### NodePort 지정

```yaml
kind: Service
spec:
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 31221   <--- 여기로 보내면 이 App이 요청을 받을 수 있게 
```

### Traffic Routing

- Pod 이중화 설정

    ```yaml
    kind: Deployment
    spec:
      replicas: 2
    ```

- Service는 기본적으로 파드가 2개 있으면 트래픽을 두 파드에 골고루 로드밸런싱해줌

### AutoScaling

```yaml
kind: HorizontalPodAutoscaler
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: app-1-2-2-1
  minReplicas: 2
  maxReplicas: 4
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 40
```

- AutoScaler 설정
    - min ~ max : 2 ~ 4 개로 설정
    - metrics: AutoScaling 지표

### Self-Healing

메모리 Leak 등으로 파드가 죽는 경우 발생 시 Kubernetes는 App의 트래픽을 죽은 파드로 보내지 않고 새로운 파드를 다시 살린 후에 다시 Traffic Routing을 한다.

### RollingUpdate

이미지 업데이트 시 돌아가면서 이미지를 순차적으로 업데이트 한다. 역시 업데이트 하는 동안 트래픽은 해당 서버로 보내지 않는다.

# 3) 쿠버네티스가 서비스 안정화에 기여하는 방법

- 환경의 자동설정으로 누락 등의 휴먼에러를 줄여줌
- 인프라 히스토리를 yaml 히스토리로 관리할 수 있음
- 환경별로 인프라 세팅 파일을 나눌 수 있음