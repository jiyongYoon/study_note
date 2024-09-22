# 9. Service

---

<img src="https://github.com/user-attachments/assets/4a078f2a-ac4e-4a5a-b402-5567486813dd" alt="adder" width="100%" />

- Pod 운영하는 관리자 느낌임…
- 여러 역할을 하고있어 개념이 좀 헷갈릴 수 있음

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

# 1. Service Object의 역할

## 1) 서비스 퍼블리싱

- 외부에서 Pod로 트래픽을 연결해주는 기능

## 2) 서비스 디스커버리

- 쿠버네티스 내부 DNS를 이용해서 서비스의 이름을 api로 호출할 수 있게 해주는 기능. (docker network 이름이랑 비슷한 느낌쓰)

## 3) 서비스 레지스트리

- 서비스 내에 생성된 Pod의 IP를 관리하며, 외부 트래픽의 로드밸런싱 역할을 함께 함

# 2. 필드 설명

## spec.selector

- 연결될 Pod를 특정한다.

## spec.ports

### port (required = true)

- 서비스 이름으로 api 호출 시 해당 port를 사용하면 됨
    - 동일 Namespace의 Pod에서 호출 시 - `http://{서비스이름}:{port}/` 로 호출 가능
    - 다른 Namespace의 Pod에서 호출 시 - `http://{서비스이름}.{Namespace}:{port}/`로 호출 가능

### targetPort

- 두 가지로 사용될 수 있음
    1. Pod 내의 container에 기동하고 있는 App의 port번호 직접 기입 → App port에 의존적임
    2. Pod의 `containers.ports.name`과 바인딩 되어 사용 가능 → App port가 바뀌어도 Pod 정보만 바뀌면 됨

### nodePort

- 외부에서 Pod로 트래픽 연결시 사용 (`서비스 퍼블리싱`)
- `http://192.168.56.30:{nodePort}/`로 호출 가능

## spec.type

- 기본값은 `ClusterIP` - 쿠버네티스 내부 Pod에서만 접근하는 용도 (docker expose 비슷한 느낌이구만)
- `NodePort` - 외부에서 접근 가능하도록 하는 용도

# 3. 실습

## 1) 서비스 명으로 api 호출

### (1) namespace 외부에서 호출

```bash
[root@k8s-master 1231]# curl http://api-tester-1231:80/version
curl: (6) Could not resolve host: api-tester-1231

[root@k8s-master 1231]# curl http://192.168.56.30:31231/version
[App Version] : Api Tester v1.0.0
```

### (2) namespace 내부에서 호출

```bash
bash-4.4# curl http://api-tester-1231:80/version
[App Version] : Api Tester v1.0.0
```

### (3) namespace 외부에서 호출 (다른 namespace)

```bash
bash-4.4# curl http://api-tester-1231:80/version
curl: (6) Could not resolve host: api-tester-1231

# 서비스명.namespace명:포트
bash-4.4# curl http://api-tester-1231.anotherclass-123:80/version
[App Version] : Api Tester v1.0.0
```