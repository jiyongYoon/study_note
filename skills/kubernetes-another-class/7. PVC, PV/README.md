# 7. PVC(Persistent Volume Claim), PV(Persistent Volume)

---

<img src="https://github.com/user-attachments/assets/de8c979f-ce58-4ce8-9ca3-5b78916cc2aa" alt="adder" width="100%" />

- Pod의 volumes 를 잡아주는 역할을 함
- `PV`는 Cluster Level의 Object이며, `PVC`는 Namespace Level Object 이다.
    - `PV`는 인프라 담당자가 사용할 volume mount 솔루션에 맞게 미리 설정을 해두는 용도로 사용하며
    - `PVC`는 개발자가 `PV`를 사용할 때 인터페이스 역할을 해주게 된다.

# 1) PV (Persistent Volume)

[퍼시스턴트 볼륨](https://kubernetes.io/ko/docs/concepts/storage/persistent-volumes/)

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

## 필드 설명

### capacity (required = true)

- `storage` - 용량

### accessModes (required = true)

- 접근 모드

  **`ReadWriteOnce`**

  하나의 노드에서 해당 볼륨이 읽기-쓰기로 마운트 될 수 있다. ReadWriteOnce 접근 모드에서도 파드가 동일 노드에서 구동되는 경우에는 복수의 파드에서 볼륨에 접근할 수 있다.

  **`ReadOnlyMany`**

  볼륨이 다수의 노드에서 읽기 전용으로 마운트 될 수 있다.

  **`ReadWriteMany`**

  볼륨이 다수의 노드에서 읽기-쓰기로 마운트 될 수 있다.

  **`ReadWriteOncePod`**

  볼륨이 단일 파드에서 읽기-쓰기로 마운트될 수 있다. 전체 클러스터에서 단 하나의 파드만 해당 PVC를 읽거나 쓸 수 있어야하는 경우 ReadWriteOncePod 접근 모드를 사용한다. 이 기능은 CSI 볼륨과 쿠버네티스 버전 1.22+ 에서만 지원된다.


### local

- `path`- 노드의 volume path
- 이 값이 있다면 `nodeAffinity`도 반드시 있어야함 (어떤 노드인지 특정해주어야 하기 때문)
- 만약 특정 Node에 데이터를 저장하지 않고 Solution을 사용한다면 이 부분은 사용되지 않을 수 있다.

### nodeAffinity

```yaml
nodeAffinity:
    required:
      nodeSelectorTerms:
        - matchExpressions:
            - {key: kubernetes.io/hostname, operator: In, values: [k8s-master]}
```

- 이 값이 있는 `PV`를 사용하는 `PVC`가 잡혀있는 `Pod`는 해당 위치에 Pod가 생성되어야 함
    - 생각해보면, 그 Node에 반드시 이 경로가 있어야 하기 때문에 당연하다.
- `required.nodeSelectorTerms`
    - `- matchExpressions` - key, operation, value로 구성된 형태를 가지고 있으며, `key + value`로 Node를 특정한다.

# 2) PVC (Persistent Volume Claim)

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

### resources (required = true)

- `storage`, `accessMode`는 `PV` Object의 내용과 동일해야한다.

### selector (required = true)

- 사용할 `PV` Object를 특정한다.

### Pod와의 연결

- `PVC` 자신은 `Pod` Object의 `volumes` 필드로 선택된다.

    ```yaml
    # Deployment의 yaml 중 volume과 관련된 부분에 pvc 적용
    spec:
      template:
        spec:
    	    containers:
            - name: api-tester-1231
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

    - `volumes.name`은 container의 `volumeMounts.name`과 바인딩된다.
    - 결국, Pod 내부의 `volumeMounts.mountPath`와 `PVC` → `PV`의 `spec.local.path` 가 마운트 되게 되는 것이다.

# 3) Pod의 hostPath와 local과의 차이점

```yaml
# Deployment의 yaml 중 volume과 관련된 부분에 hostPath 적용
spec:
  template:
    spec:
      containers:
        - name: api-tester-1231
          volumeMounts:
            - name: files
              mountPath: /usr/src/myapp/files/dev
            - name: secret-datasource
              mountPath: /usr/src/myapp/datasource
      volumes:
        - name: files
          hostPath:
            path: /root/k8s-local-volume/1231
        - name: secret-datasource
          secret:
            secretName: api-tester-1231-postgresql
```

- `PVC`를 거치지 않고 직접 `hostPath`로 마운팅해준 모습

[스토리지로 퍼시스턴트볼륨(PersistentVolume)을 사용하도록 파드 설정하기](https://kubernetes.io/ko/docs/tasks/configure-pod-container/configure-persistent-volume-storage/#퍼시스턴트볼륨-생성하기)

> 로컬 테스트로 사용하는건 괜찮지만, 운영 클러스터에서는 `hostPath`를 사용하지 않는 것을 권고한다. 이유는 여러 Pod에서 접근이 불가능하며, 쿠버네티스에서는 Node 또한 운영 중 언제든 죽을 수 있는 대상으로 보기 때문이다. (노드 공간 부족 등)
따라서 데이터를 직접 저장하는 용도로 사용하기보다는 저장된 데이터를 `읽는` 용도로 사용하는 것이 적합하다.
> 
> (e. g. Loki의 promtail pod가 마운트 된 볼륨에 접근하여 Read 하고 있음)