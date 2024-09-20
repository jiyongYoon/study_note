# 8. Deployment

---

<img src="https://github.com/user-attachments/assets/529d1049-6723-4936-a364-bb4ba47cc7d5" alt="adder" width="100%" />

- Pod를 생성(`ReplicaSet`을 통해)하고 업데이트 하는 역할을 함
- `template` 부분이 변경되면 업데이트가 진행됨 (refresh and apply 된다고 생각하면 됨)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  namespace: anotherclass-123
  name: api-tester-1231
  labels:
    part-of: k8s-anotherclass
    component: backend-server
    name: api-tester
    instance: api-tester-1231
    version: 1.0.0
    managed-by: dashboard
spec:
  selector:
    matchLabels:
      part-of: k8s-anotherclass
      component: backend-server
      name: api-tester
      instance: api-tester-1231
  replicas: 2
  strategy:
    type: RollingUpdate
  template:
    metadata:
      labels:
        part-of: k8s-anotherclass
        component: backend-server
        name: api-tester
        instance: api-tester-1231
        version: 1.0.0
    spec:
      nodeSelector:
        kubernetes.io/hostname: k8s-master
      containers:
        - name: api-tester-1231
          image: 1pro/api-tester:v1.0.0
          ports:
          - name: http
            containerPort: 8080
          envFrom:
            - configMapRef:
                name: api-tester-1231-properties
          startupProbe:
            httpGet:
              path: "/startup"
              port: 8080
            periodSeconds: 5
            failureThreshold: 36
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
            periodSeconds: 10
            failureThreshold: 3
          resources:
            requests:
              memory: "100Mi"
              cpu: "100m"
            limits:
              memory: "200Mi"
              cpu: "200m"
          volumeMounts:
            - name: files
              mountPath: /usr/src/myapp/files/dev
            - name: secret-datasource
              mountPath: /usr/src/myapp/datasource
      volumes:
        - name: files
          persistentVolumeClaim:
            claimName: api-tester-1231-files
        - name: secret-datasource
          secret:
            secretName: api-tester-1231-postgresql
```

# 1. 필드 설명

## selector

- ReplicaSet과 바인딩

## replicas

- 최초 생성할 Pod 개수
- HPA에 숫자가 다르면 HPA 숫자가 적용된다.

## strategy

### type

- Pod 생성 전략을 명시함
- `Recreate`
    - Pod 생성 시 기존에 동작하던 Pod는 모두 종료시키고 새로 Pod를 생성한다. 따라서 새 Pod가 기동될 때까지 서비스 중단 시간이 발생한다.
- `RollingUpdate`
    - Pod를 순차적으로 생성한다. 순차적 생성 기준은 아래 옵션을 기준으로 결정한다.
        - `maxUnavailable` - 기본값 25(%). 업데이트 동안 최대 몇 개의 Pod를 서비스 불가 상태로 유지할 것인가?
        - `maxSurge` - 기본값 25(%). 업데이트 동안 새 Pod를 최대 몇개까지 동시에 만들 것인가?
    - 사용 예시는 위 그림을 참고할 것

## template

- 업데이트 되면 Pod가 재기동 되는 블럭

### spec.container.envFrom

- `configMapRef.name` - 사용할 configMap의 이름

### spec.container.xxxProbe

- 앱 상태 진단에 사용할 Probe
- 자세한 내용

  [5. Probe](../5.%20Probe/README.md)


### spec.container.resources

- Pod가 할당받을 Node의 computing 자원
    - `requests` - `HPA` Object 가 AutoScaling을 할 때 100% 기준이 되는 값
    - `limits` - 실제 컨테이너가 가용할 수 있는 자원의 최대 범위

### spec.container.volumeMounts

- 컨테이너 내부의 볼륨
- `name` - `spec.volumes` 필드의 name과 매칭됨

### spec.volumes

- 컨테이너 외부의 마운트 될 볼륨
- `PVC` ,`hostPath` , `secret` 사용 가능

# 2. RollingUpdate 테스트

## 1) 버전 호출을 통해 업데이트 시점 확인 (api 만들어놓음)

```bash
while true; do curl http://192.168.56.30:31231/version; sleep 2; echo ''; done; 
```

## 2) strategy 수정

- strategy는 template 블럭이 아니기 때문에 대시보드에서 수정 후 업데이트 하여도 바로 적용되지 않음.

### (1) RollingUpdate 실행해보기

- 적용 yaml (max 필드 생략하면 25% 기본값으로 실행)

    ```yaml
    spec:
      strategy:
        type: RollingUpdate
    ```

- 적용 명령어

    ```bash
    [root@k8s-master ~]# kubectl set image -n anotherclass-123 deployment/api-tester-1231 api-tester-1231=1pro/api-tester:v2.0.0
    deployment.apps/api-tester-1231 image updated
    ```

- 호출 확인

    ```bash
    [root@k8s-master 1231]# while true; do curl http://192.168.56.30:31231/version; sleep 2; echo ''; done;
    
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    ```

- 흐름
    1. Pod가 새로 하나 생성됨 (Deployment 파드 3/2)
    2. startUpProbe 동작 및 성공
    3. livenessProbe, readinessProbe 동작 및 성공과 동시에 v1.0.0 기존 Pod 한 개 정지 (Deployment 파드 2/2)
    4. Api Tester v2.0.0 호출 되기 시작함
        - v1.0.0 pod와 v2.0.0 pod가 한 개씩 있는 상태이므로 번갈아가면서 뜸
    5. Pod 하나가 또 기동됨 (Deployment 파드 3/2)
    6. 2 ~ 4번 과정 반복 (Deployment 파드 2/2)

### (2) RollingUpdate (maxUnavailable: 0%, maxSurge: 100%) 로 실행해보기

- 적용 yaml

    ```yaml
    spec:
      strategy:
        type: RollingUpdate
        rollingUpdate:
          maxUnavailable: 0%
          maxSurge: 100%
    ```

- 적용 명령어

    ```bash
    [root@k8s-master 1231]# kubectl set image -n anotherclass-123 deployment/api-tester-1231 api-tester-1231=1pro/api-tester:v1.0.0
    deployment.apps/api-tester-1231 image updated
    ```

- 호출 확인

    ```bash
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    ```

- 흐름
    1. Pod 2개가 생성됨 (Deployment 파드 4/2)
    2. startUpProbe 동작 및 성공
    3. livenessProbe, readinessProbe 동작 및 성공과 동시에 v2.0.0 기존 Pod 정지 (성공한 Pod 개수만큼)
    4. Api Tester v1.0.0 호출 되기 시작함
        - 두 Pod의 기동시간이 거의 동일하여 마치 blue - green 처럼 한 번에 switch 되는 것처럼 동작됨

    ```bash
    2024-09-10 18:01:00	
    2024-09-10 18:01:00	  .   ____          _            __ _ _
    2024-09-10 18:01:00	 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
    2024-09-10 18:01:00	( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
    2024-09-10 18:01:00	 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
    2024-09-10 18:01:00	  '  |____| .__|_| |_|_| |_\__, | / / / /
    2024-09-10 18:01:00	 =========|_|==============|___/=/_/_/_/
    2024-09-10 18:01:00	 :: Spring Boot ::                (v3.1.0)
    2024-09-10 18:01:00	
    2024-09-10 18:01:00	
    2024-09-10 18:01:00	  .   ____          _            __ _ _
    2024-09-10 18:01:00	 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
    2024-09-10 18:01:00	( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
    2024-09-10 18:01:00	 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
    2024-09-10 18:01:00	  '  |____| .__|_| |_|_| |_\__, | / / / /
    2024-09-10 18:01:00	 =========|_|==============|___/=/_/_/_/
    2024-09-10 18:01:00	 :: Spring Boot ::                (v3.1.0)
    2024-09-10 18:01:00	
    2024-09-10 18:01:00	2024-09-10T09:01:00.378Z  INFO 1 --- [           main] com.pro.app.AppApplication               : Starting AppApplication v0.0.1-SNAPSHOT using Java 17.0.2 with PID 1 (/usr/src/myapp/app.jar started by root in /usr/src/myapp)
    2024-09-10 18:01:00	2024-09-10T09:01:00.379Z  INFO 1 --- [           main] com.pro.app.AppApplication               : The following 1 profile is active: "dev"
    2024-09-10 18:01:00	2024-09-10T09:01:00.416Z  INFO 1 --- [nio-8080-exec-2] DefaultService                           : [Kubernetes] livenessProbe is Succeed-> [System] isAppLive: true
    2024-09-10 18:01:00	2024-09-10T09:01:00.579Z  INFO 1 --- [           main] com.pro.app.AppApplication               : Starting AppApplication v0.0.1-SNAPSHOT using Java 17.0.2 with PID 1 (/usr/src/myapp/app.jar started by root in /usr/src/myapp)
    2024-09-10 18:01:00	2024-09-10T09:01:00.581Z  INFO 1 --- [           main] com.pro.app.AppApplication               : The following 1 profile is active: "dev"
    2024-09-10 18:01:06	2024-09-10T09:01:06.255Z  INFO 1 --- [nio-8080-exec-2] DefaultService                           : [Kubernetes] livenessProbe is Succeed-> [System] isAppLive: true
    2024-09-10 18:01:06	2024-09-10T09:01:06.986Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
    2024-09-10 18:01:06	2024-09-10T09:01:06.990Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
    2024-09-10 18:01:07	2024-09-10T09:01:07.086Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
    2024-09-10 18:01:07	2024-09-10T09:01:07.086Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.8]
    2024-09-10 18:01:07	2024-09-10T09:01:07.092Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
    2024-09-10 18:01:07	2024-09-10T09:01:07.093Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.8]
    2024-09-10 18:01:07	2024-09-10T09:01:07.993Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
    2024-09-10 18:01:08	2024-09-10T09:01:08.178Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 7313 ms
    2024-09-10 18:01:08	2024-09-10T09:01:08.179Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
    2024-09-10 18:01:08	2024-09-10T09:01:08.181Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 7298 ms
    2024-09-10 18:01:10	2024-09-10T09:01:10.416Z  INFO 1 --- [nio-8080-exec-3] DefaultService                           : [Kubernetes] livenessProbe is Succeed-> [System] isAppLive: true
    2024-09-10 18:01:12	2024-09-10T09:01:12.405Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
    2024-09-10 18:01:12	2024-09-10T09:01:12.478Z  INFO 1 --- [           main] Startup                                  : [System] App is initializing
    2024-09-10 18:01:12	2024-09-10T09:01:12.585Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
    2024-09-10 18:01:12	2024-09-10T09:01:12.780Z  INFO 1 --- [           main] Startup                                  : [System] App is initializing
    2024-09-10 18:01:16	2024-09-10T09:01:16.253Z  INFO 1 --- [nio-8080-exec-7] DefaultService                           : [Kubernetes] livenessProbe is Succeed-> [System] isAppLive: true
    2024-09-10 18:01:16	2024-09-10T09:01:16.885Z  INFO 1 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
    2024-09-10 18:01:16	2024-09-10T09:01:16.886Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
    2024-09-10 18:01:16	2024-09-10T09:01:16.886Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
    2024-09-10 18:01:17	2024-09-10T09:01:17.077Z  INFO 1 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
    2024-09-10 18:01:17	2024-09-10T09:01:17.078Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
    2024-09-10 18:01:17	2024-09-10T09:01:17.078Z  INFO 1 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
    2024-09-10 18:01:17	2024-09-10T09:01:17.091Z  INFO 1 --- [nio-8080-exec-1] DefaultService                           : [Kubernetes] startupProbe is Failed-> [System] isAppLive: false
    2024-09-10 18:01:17	2024-09-10T09:01:17.186Z  INFO 1 --- [nio-8080-exec-1] DefaultService                           : [Kubernetes] startupProbe is Failed-> [System] isAppLive: false
    2024-09-10 18:01:17	2024-09-10T09:01:17.480Z  INFO 1 --- [           main] Startup                                  : [System] Database is connecting
    2024-09-10 18:01:17	2024-09-10T09:01:17.781Z  INFO 1 --- [           main] Startup                                  : [System] Database is connecting
    2024-09-10 18:01:20	2024-09-10T09:01:20.414Z  INFO 1 --- [nio-8080-exec-8] DefaultService                           : [Kubernetes] livenessProbe is Succeed-> [System] isAppLive: true
    2024-09-10 18:01:21	2024-09-10T09:01:21.754Z  INFO 1 --- [nio-8080-exec-2] DefaultService                           : [Kubernetes] startupProbe is Failed-> [System] isAppLive: false
    2024-09-10 18:01:21	2024-09-10T09:01:21.983Z  INFO 1 --- [nio-8080-exec-2] DefaultService                           : [Kubernetes] startupProbe is Failed-> [System] isAppLive: false
    2024-09-10 18:01:22	2024-09-10T09:01:22.499Z  INFO 1 --- [           main] Startup                                  : [System] Database is connected
    2024-09-10 18:01:22	2024-09-10T09:01:22.783Z  INFO 1 --- [           main] Startup                                  : [System] Database is connected
    ```


### (3) Recreate 실행해보기

- 적용 yaml

    ```yaml
    strategy:
      type: Recreate
    ```

- 적용 명령어

    ```bash
    [root@k8s-master ~]# kubectl set image -n anotherclass-123 deployment/api-tester-1231 api-tester-1231=1pro/api-tester:v2.0.0
    deployment.apps/api-tester-1231 image updated
    ```

- 호출 확인

    ```bash
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    [App Version] : Api Tester v1.0.0
    curl: (7) Failed to connect to 192.168.56.30 port 31231: Connection refused
    
    curl: (7) Failed to connect to 192.168.56.30 port 31231: Connection refused
    
    curl: (7) Failed to connect to 192.168.56.30 port 31231: Connection refused
    
    curl: (7) Failed to connect to 192.168.56.30 port 31231: Connection refused
    
    curl: (7) Failed to connect to 192.168.56.30 port 31231: Connection refused
    
    curl: (7) Failed to connect to 192.168.56.30 port 31231: Connection refused
    
    curl: (7) Failed to connect to 192.168.56.30 port 31231: Connection refused
    
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    [App Version] : Api Tester v2.0.0
    ```

- 흐름
    1. 기존 Pod가 모두 내려가며, 새로운 버전의 Pod가 2개 동시에 생성됨 (Deployment 파드 2/2)
    2. startUpProbe, livenessProbe, readinessProbe가 모두 성공하면 v2.0.0이 호출됨 (Deployment 파드 2/2)