# 4. 쿠버네티스 오브젝트 개요

---

# 1. Label이란?

Object의 정보를 제공하는 역할을 하며, 이 정보를 기반으로 Selector를 사용해 Object들을 연결시켜주는 용도로 사용할 수 있음.

## Label에 들어가는 정보들

- `part-of` - 애플리케이션의 전체 이름
- `component` - 구성요소 (전체 애플리케이션에서 해당 Object의 역할)
- `name` - 애플리케이션의 낱개 이름
- `instance` - 인스턴스 식별자. 보통 (name-instance) 로 식별자를 만드는 경우가 일반적임.
- `version` - 버전

> Lable의 필드 앞에는 도메인 Prefix가 붙는 경우도 있다.
> 
> 예시) <br>
> app.kubernetes.io/part-of: dummy <br>
> app.kubernetes.io/component: dummy <br>
> app.kubernetes.io/name: dummy <br>
> …

Selector와 매칭되어 Object 연결 시 사용하기 위해서는 Selector는 Label보다 많은 구성요소(정보)를 가질 수 없다.

<img src="https://github.com/user-attachments/assets/76c5f505-2fef-428d-90dc-ff1270227509" alt="adder" width="80%" />

- 적용 예시
    - `Deployment` 오브젝트가 `ReplicaSet`과 연결되고 싶은 경우, `Deployment`의 `Selector`에 `ReplicaSet`의 `Label` 정보를 넣어주면 된다는 뜻이다. 단, `ReplicaSet`의 `Label` 보다 더 많은 정보를 가지면 정보가 다르기 때문에 연결에 실패한다.
    - 보통 `instance` 필드는 유일한 식별자를 가지기 때문에 이 필드만 넣어서 매칭을 해도 된다.

Label에 추가되는 정보에 따라서 ‘정보성 라벨링’, ‘정보 + 기능성 라벨링’ 으로 개념적으로 구분지어 볼 수 있다.

- 정보성 라벨링 예시

    ```yaml
    labels:
      part-of: k8s-anotherclass
      managed-by: dashboard
    ```

- 정보 + 기능성 라벨링 예시

    ```yaml
    labels:
      part-of: k8s-anotherclass
      component: backend-server
      name: api-tester
      instance: api-tester-1231
      version: 1.0.0
    ```


# 2. Object의 분류

- Object는 `Cluster Level Object`와 `Namespace Level Object`로 나누어진다.
- Cluster Lever Object는 쿠버네티스 클러스터와 라이프 사이클이 같지만, Namespace Level Object는 `Namespace` Object와 라이프 사이클이 같다.

### Cluster Level Object

- `Namespace`
- `PV`

### Namespace Level Object

- `Deployment`
- `HPA`
- `ConfigMap`
- `Secret`
- `PVC`
- `Service`

# 3. 각 Object에 대한 설명과 yaml 예시

## **▶ Namespace**

- Object들을 Grouping 해줌
- 한 Namespace 안에서 같은 종류의 Object 끼리는 이름이 중복될 수 없다.

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: anotherclass-123
  labels:
    part-of: k8s-anotherclass
    managed-by: dashboard   <--- Object 생성 주체 확인 용도로 쓴다
```

## **▶ Deployment**

- Pod를 만들고, 업데이트를 해줌
    - Pod 복제본은 `ReplicaSet`이 만들어지고, 그 친구가 관리하게 된다.

### yaml 예시

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

## **▶ Service**

- Pod에 트래픽을 연결시켜줌

### yaml 예시

```yaml
apiVersion: v1
kind: Service
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
    part-of: k8s-anotherclass
    component: backend-server
    name: api-tester
    instance: api-tester-1231
  ports:
    - port: 80
      targetPort: http
      nodePort: 31231
  type: NodePort
```

## **▶ Configmap**

- Pod의 환경변수 값 제공 (docker-compose의 .env와 비슷해보인다)

### yaml 예시

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  namespace: anotherclass-123
  name: api-tester-1231-properties
  labels:
    part-of: k8s-anotherclass
    component: backend-server
    name: api-tester
    instance: api-tester-1231
    version: 1.0.0
    managed-by: dashboard
data:
  spring_profiles_active: "dev"
  application_role: "ALL"
  postgresql_filepath: "/usr/src/myapp/datasource/postgresql-info.yaml"
```

## **▶ Secret**

- Pod에 중요한 secret 값 제공

### yaml 예시

```yaml
apiVersion: v1
kind: Secret
metadata:
  namespace: anotherclass-123
  name: api-tester-1231-postgresql
  labels:
    part-of: k8s-anotherclass
    component: backend-server
    name: api-tester
    instance: api-tester-1231
    version: 1.0.0
    managed-by: dashboard
stringData:
  postgresql-info.yaml: |
    driver-class-name: "org.postgresql.Driver"
    url: "jdbc:postgresql://postgresql:5431"
    username: "dev"
    password: "dev123"
```

## **▶ PVC**

- Pod에서 PV를 지정할 때 사용됨

### yaml 예시

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  namespace: anotherclass-123
  name: api-tester-1231-files
  labels:
    part-of: k8s-anotherclass
    component: backend-server
    name: api-tester
    instance: api-tester-1231
    version: 1.0.0
    managed-by: kubectl
spec:
  resources:
    requests:
      storage: 2G
  accessModes:
    - ReadWriteMany
  selector:
    matchLabels:
      part-of: k8s-anotherclass
      component: backend-server
      name: api-tester
      instance: api-tester-1231-files
```

## **▶ PV**

- 실제 Volume을 지정함

### yaml 예시

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: api-tester-1231-files
  labels:
    part-of: k8s-anotherclass
    component: backend-server
    name: api-tester
    instance: api-tester-1231-files
    version: 1.0.0
    managed-by: dashboard
spec:
  capacity:
    storage: 2G
  volumeMode: Filesystem
  accessModes:
    - ReadWriteMany
  local:
    path: "/root/k8s-local-volume/1231"
  nodeAffinity:
    required:
      nodeSelectorTerms:
        - matchExpressions:
            - {key: kubernetes.io/hostname, operator: In, values: [k8s-master]}
```

## **▶ HPA**

- 부하에 따라 Pod 개수를 조절하는 기준 제시
- `Deployment` 오브젝트의 숫자보다 우선순위
    - ex, HPA의 minReplicas가 2면 Deployment의 minReplicas가 1이여도 2가 됨.

### yaml 예시

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