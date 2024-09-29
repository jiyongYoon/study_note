# 6. ConfigMap, Secret

---

- 두 Object는 Pod와 Pod 내의 App이 기동될 때 필요한 변수를 전달하는 역할을 한다.

<img src="https://github.com/user-attachments/assets/49674113-aee1-4107-a390-be6341f304ff" alt="adder" width="100%" />

# 1. ConfigMap

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

- Key: Value의 형태를 가진 변수 모음이며, Pod 내에 `환경변수`의 형태로 주입된다.
    - Pod 내에서 env 명령어를 통해 값을 확인할 수 있다.
- `data` 필드 뒤에 들어가게 된다.
- 보통
    - 인프라를 위한 데이터,
    - App 기능 제어를 위한 데이터,
    - 외부 환경을 App으로 주입시키기 위한 데이터

  등이 전달된다.

- `Secret`은 file path로 값을 주입한다.

# 2. Secret

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

- Base64로 인코딩하여 `volumes`를 통해 마운트 된 위치에 파일이 생성된다.
    - 실제로 암호화되는 것은 아니라는 뜻이며, 무분별한 접근 및 노출 정도를 막아주는 역할을 할 수 있음.
- `stringData` 필드 뒤에 들어가게 된다.
- 이후 filed명을 가진 file이 만들어지며, 파일을 `쓰기 전용`으로 만들어 진다.
    - 위 예제에서는 `postgresql-info.yaml` 이라는 이름을 가진 파일이 만들어짐
- `stringData` 이후에 `type`이라는 필드가 추가될 수 있다.
    - `opaque` - Base64로 인코딩되는 기본타입
    - `docker-registry` - 사설 저장소와 관련된 key - value 값을 추가하기 위해서 사용하는 타입
    - `tls` - 인증서와 관련된 key - value 값을 추가하기 위해서 사용하는 타입

# 3. ConfigMap과 Secret의 영향

- 개발 & 배포 전반의 Process에 모두 영향을 주는 Object이다 보니, 각 영역의 담당자들과 소통을 잘 해서 사용 범위와 값을 잘 정하는 것이 중요함.