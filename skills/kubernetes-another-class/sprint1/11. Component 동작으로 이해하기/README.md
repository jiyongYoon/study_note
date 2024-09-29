# 1. Kubernetes의 전체 개요

<img src="https://github.com/user-attachments/assets/003980e0-7fa0-466d-ba38-2e5bd1feba15" alt="adder" width="100%" />

## 1) VM, Node

- Node는 쿠버네티스 클러스터의 구성요소이며, 컨테이너 (Pod)를 실행하는 역할을 함. 클러스터 안에서 쿠버네티스 작업을 수행하는 하나의 단위이며, 각 노드에는 container runtime, kubelet 등이 실행되고 있음.
- Node와 VM은 완전히 동일하지는 않으며, Node가 실행되는 형태 중 하나가 VM 형태임 (Server 등 여러 형태로도 실행됨)

### (1) kubeadm

- 역할: 쿠버네티스 클러스터를 초기화하고 부트스트랩하는 도구
- 사용 목적: 클러스터 설치 자동화를 돕기 위함으로, 노드 구성 및 추가하는 과정에서 많이 사용됨
- 작동 방식: kube-apiserver, kubelet, etcd 등의 필수 쿠버네티스 컴포넌트를 설정하고 배포함
    - 주요 사용 예시
        - `kubeadm init`: 마스터 노드를 설정하고 클러스터 초기화
        - `kubeadm join`: 워커 노드를 기존 클러스터에 연결
        - `kubeadm reset`: 클러스터 초기화 또는 연결을 제거

### (2) kubectl

- 역할: 쿠버네티스 클러스터와 상호작용하는 CLI 도구
- 사용 목적: 쿠버네티스 클러스터의 리소스(e.g. Pod, Service, Deployment 등)을 관리하고 제어할 때 사용됨
- 작동 방식: kube-apiserver 와 통신하여 클러스터 작업을 처리함.
    - 주요 사용 예시
        - `kubectl get pods`: 클러스터의 모든 Pod 목록을 조회
        - `kubectl apply -f deployment.yaml`: YAML 파일에 정의된 리소스를 클러스터에 적용
        - `kubectl delete pod my-pod`: 특정 Pod 삭제

### (3) kubelet

- 역할: 컨테이너의 실행과 상태를 관리하는 에이전트. 각 노드에서 실행됨
- 사용 목적: 노드에서 Pod와 그 안의 컨테이너를 실행하고 동작을 모니터링 및 관리함
- 작동 방식: PodSpec(Pod의 구성 파일)을 받아들여, 그에 따라 컨테이너를 실행하고 Pode의 상태를 지속적으로 체크하여 kube-apiserver에 보고함
    - 주요 역할
        - Pod가 정의된 대로 실행되고 있는지 확인
        - 컨테이너가 제대로 실행되지 않으면 재시작
        - 노드에서 Pod의 리소스 사용량(CPU, 메모리 등)을 모니터링하고 kube-apiserver로 보고

## 2) Kubernetes Cluster

- Control Plane Component 들로 구성되어 쿠버네티스 클러스터를 관리함
    - 따라서 대부분의 Control Plane Component 들은 Master Node에서 실행되나, `kube-proxy`는 모든 노드에서 실행됨
- Pod 형태로 실행됨
- `kube-apiserver`, `etcd`, `kube-controller-manager`, `kube-scheduler` , `kube-proxy`등이 포함됨

### (1) kube-apiserver

- 쿠버네티스의 중앙 통신 허브
- 사용자가 전달하는 명령어는 `kube-apiserver`에 전달되어 처리된다.

### (2) etcd

- 분산 key-value 저장소
- 클러스터의 모든 상태 정보를 저장
    - 모든 리소스(Pod, Service, ConfigMap 등)의 상태와 데이터 저장
    - 분산 저장소로 작동하여 데이터 무결성 보장

### (3) kube-controller-manager

- 클러스터의 상태 모니터링
- 대표적인 컨트롤러들:
    - **Replication Controller**: 지정된 수의 Pod가 항상 실행되도록 보장
    - **Node Controller**: 노드의 상태를 모니터링하고, 장애가 발생하면 대응
    - **Job Controller**: 지정된 작업(Job)이 성공적으로 완료될 때까지 Pod를 관리
    - **Service Controller**: 클러스터 내에서 서비스 리소스 관리

### (4) kube-scheduler

- 새로 생성된 Pod를 적절한 노드에 배치하는 역할
- 노드 자원을 모니터링하고 있음

### (5) kube-proxy

- 클러스터 내에서 네트워크 통신을 관리하는 컴포넌트
- 서비스 간의 트래픽 라우팅, 서비스 디스커버리 지원

## 3) Resource

- 쿠버네티스 클러스터 내에서(etcd) 관리되고 소비되는 모든 개체를 의미함

| 항목 | Level | 역할 |
| --- | --- | --- |
| Namespace | Cluster |  |
| HPA | Namespace | Controller |
| Deployment | Namespace | Controller |
| ReplicaSet | Namespace | Controller |
| Service | Namespace | Object |
| ConfigMap | Namespace | Object |
| Secret | Namespace | Object |
| Pod | Namespace | Object |
| PVC | Namespace | Object |
| PV | Cluster |  |

# 2. Kubernetes 전체 구성요소들의 동작 흐름

## 1) Pod 생성 및 probe 동작

<img src="https://github.com/user-attachments/assets/2cb88dd1-2543-49a1-b492-12c13f1c13ab" alt="adder" width="100%" />

## 2) Service 동작

<img src="https://github.com/user-attachments/assets/19b9788b-3a17-4c1a-b147-c0b39eff12e9" alt="adder" width="100%" />

## 3) Secret 동작

<img src="https://github.com/user-attachments/assets/6b5d46f7-f63e-4789-a732-033c135ed4a5" alt="adder" width="100%" />

## 4) HPA 동작

<img src="https://github.com/user-attachments/assets/372c9c16-bc14-432f-937a-ff64b3ae6795" alt="adder" width="100%" />

# 3. Resource 확인

## 1) 모든 Resource 확인

```bash
kubectl api-resources
```

```bash

[root@k8s-master kubernetes]# kubectl api-resources
NAME                              SHORTNAMES                                      APIVERSION                             NAMESPACED   KIND
bindings                                                                          v1                                     true         Binding
componentstatuses                 cs                                              v1                                     false        ComponentStatus
configmaps                        cm                                              v1                                     true         ConfigMap
endpoints                         ep                                              v1                                     true         Endpoints
events                            ev                                              v1                                     true         Event
limitranges                       limits                                          v1                                     true         LimitRange
namespaces                        ns                                              v1                                     false        Namespace
nodes                             no                                              v1                                     false        Node
persistentvolumeclaims            pvc                                             v1                                     true         PersistentVolumeClaim
persistentvolumes                 pv                                              v1                                     false        PersistentVolume
pods                              po                                              v1                                     true         Pod
podtemplates                                                                      v1                                     true         PodTemplate
replicationcontrollers            rc                                              v1                                     true         ReplicationController
resourcequotas                    quota                                           v1                                     true         ResourceQuota
secrets                                                                           v1                                     true         Secret
serviceaccounts                   sa                                              v1                                     true         ServiceAccount
services                          svc                                             v1                                     true         Service
mutatingwebhookconfigurations                                                     admissionregistration.k8s.io/v1        false        MutatingWebhookConfiguration
validatingwebhookconfigurations                                                   admissionregistration.k8s.io/v1        false        ValidatingWebhookConfiguration
customresourcedefinitions         crd,crds                                        apiextensions.k8s.io/v1                false        CustomResourceDefinition
apiservices                                                                       apiregistration.k8s.io/v1              false        APIService
controllerrevisions                                                               apps/v1                                true         ControllerRevision
daemonsets                        ds                                              apps/v1                                true         DaemonSet
deployments                       deploy                                          apps/v1                                true         Deployment
replicasets                       rs                                              apps/v1                                true         ReplicaSet
statefulsets                      sts                                             apps/v1                                true         StatefulSet
tokenreviews                                                                      authentication.k8s.io/v1               false        TokenReview
localsubjectaccessreviews                                                         authorization.k8s.io/v1                true         LocalSubjectAccessReview
selfsubjectaccessreviews                                                          authorization.k8s.io/v1                false        SelfSubjectAccessReview
selfsubjectrulesreviews                                                           authorization.k8s.io/v1                false        SelfSubjectRulesReview
subjectaccessreviews                                                              authorization.k8s.io/v1                false        SubjectAccessReview
horizontalpodautoscalers          hpa                                             autoscaling/v2                         true         HorizontalPodAutoscaler
cronjobs                          cj                                              batch/v1                               true         CronJob
jobs                                                                              batch/v1                               true         Job
certificatesigningrequests        csr                                             certificates.k8s.io/v1                 false        CertificateSigningRequest
leases                                                                            coordination.k8s.io/v1                 true         Lease
bgpconfigurations                                                                 crd.projectcalico.org/v1               false        BGPConfiguration
bgpfilters                                                                        crd.projectcalico.org/v1               false        BGPFilter
bgppeers                                                                          crd.projectcalico.org/v1               false        BGPPeer
blockaffinities                                                                   crd.projectcalico.org/v1               false        BlockAffinity
caliconodestatuses                                                                crd.projectcalico.org/v1               false        CalicoNodeStatus
clusterinformations                                                               crd.projectcalico.org/v1               false        ClusterInformation
felixconfigurations                                                               crd.projectcalico.org/v1               false        FelixConfiguration
globalnetworkpolicies                                                             crd.projectcalico.org/v1               false        GlobalNetworkPolicy
globalnetworksets                                                                 crd.projectcalico.org/v1               false        GlobalNetworkSet
hostendpoints                                                                     crd.projectcalico.org/v1               false        HostEndpoint
ipamblocks                                                                        crd.projectcalico.org/v1               false        IPAMBlock
ipamconfigs                                                                       crd.projectcalico.org/v1               false        IPAMConfig
ipamhandles                                                                       crd.projectcalico.org/v1               false        IPAMHandle
ippools                                                                           crd.projectcalico.org/v1               false        IPPool
ipreservations                                                                    crd.projectcalico.org/v1               false        IPReservation
kubecontrollersconfigurations                                                     crd.projectcalico.org/v1               false        KubeControllersConfiguration
networkpolicies                                                                   crd.projectcalico.org/v1               true         NetworkPolicy
networksets                                                                       crd.projectcalico.org/v1               true         NetworkSet
endpointslices                                                                    discovery.k8s.io/v1                    true         EndpointSlice
events                            ev                                              events.k8s.io/v1                       true         Event
flowschemas                                                                       flowcontrol.apiserver.k8s.io/v1beta3   false        FlowSchema
prioritylevelconfigurations                                                       flowcontrol.apiserver.k8s.io/v1beta3   false        PriorityLevelConfiguration
nodes                                                                             metrics.k8s.io/v1beta1                 false        NodeMetrics
pods                                                                              metrics.k8s.io/v1beta1                 true         PodMetrics
alertmanagerconfigs               amcfg                                           monitoring.coreos.com/v1alpha1         true         AlertmanagerConfig
alertmanagers                     am                                              monitoring.coreos.com/v1               true         Alertmanager
podmonitors                       pmon                                            monitoring.coreos.com/v1               true         PodMonitor
probes                            prb                                             monitoring.coreos.com/v1               true         Probe
prometheusagents                  promagent                                       monitoring.coreos.com/v1alpha1         true         PrometheusAgent
prometheuses                      prom                                            monitoring.coreos.com/v1               true         Prometheus
prometheusrules                   promrule                                        monitoring.coreos.com/v1               true         PrometheusRule
scrapeconfigs                     scfg                                            monitoring.coreos.com/v1alpha1         true         ScrapeConfig
servicemonitors                   smon                                            monitoring.coreos.com/v1               true         ServiceMonitor
thanosrulers                      ruler                                           monitoring.coreos.com/v1               true         ThanosRuler
ingressclasses                                                                    networking.k8s.io/v1                   false        IngressClass
ingresses                         ing                                             networking.k8s.io/v1                   true         Ingress
networkpolicies                   netpol                                          networking.k8s.io/v1                   true         NetworkPolicy
runtimeclasses                                                                    node.k8s.io/v1                         false        RuntimeClass
apiservers                                                                        operator.tigera.io/v1                  false        APIServer
imagesets                                                                         operator.tigera.io/v1                  false        ImageSet
installations                                                                     operator.tigera.io/v1                  false        Installation
tigerastatuses                                                                    operator.tigera.io/v1                  false        TigeraStatus
poddisruptionbudgets              pdb                                             policy/v1                              true         PodDisruptionBudget
bgpconfigurations                 bgpconfig,bgpconfigs                            projectcalico.org/v3                   false        BGPConfiguration
bgpfilters                                                                        projectcalico.org/v3                   false        BGPFilter
bgppeers                                                                          projectcalico.org/v3                   false        BGPPeer
blockaffinities                   blockaffinity,affinity,affinities               projectcalico.org/v3                   false        BlockAffinity
caliconodestatuses                caliconodestatus                                projectcalico.org/v3                   false        CalicoNodeStatus
clusterinformations               clusterinfo                                     projectcalico.org/v3                   false        ClusterInformation
felixconfigurations               felixconfig,felixconfigs                        projectcalico.org/v3                   false        FelixConfiguration
globalnetworkpolicies             gnp,cgnp,calicoglobalnetworkpolicies            projectcalico.org/v3                   false        GlobalNetworkPolicy
globalnetworksets                                                                 projectcalico.org/v3                   false        GlobalNetworkSet
hostendpoints                     hep,heps                                        projectcalico.org/v3                   false        HostEndpoint
ipamconfigurations                ipamconfig                                      projectcalico.org/v3                   false        IPAMConfiguration
ippools                                                                           projectcalico.org/v3                   false        IPPool
ipreservations                                                                    projectcalico.org/v3                   false        IPReservation
kubecontrollersconfigurations                                                     projectcalico.org/v3                   false        KubeControllersConfiguration
networkpolicies                   cnp,caliconetworkpolicy,caliconetworkpolicies   projectcalico.org/v3                   true         NetworkPolicy
networksets                       netsets                                         projectcalico.org/v3                   true         NetworkSet
profiles                                                                          projectcalico.org/v3                   false        Profile
clusterrolebindings                                                               rbac.authorization.k8s.io/v1           false        ClusterRoleBinding
clusterroles                                                                      rbac.authorization.k8s.io/v1           false        ClusterRole
rolebindings                                                                      rbac.authorization.k8s.io/v1           true         RoleBinding
roles                                                                             rbac.authorization.k8s.io/v1           true         Role
priorityclasses                   pc                                              scheduling.k8s.io/v1                   false        PriorityClass
csidrivers                                                                        storage.k8s.io/v1                      false        CSIDriver
csinodes                                                                          storage.k8s.io/v1                      false        CSINode
csistoragecapacities                                                              storage.k8s.io/v1                      true         CSIStorageCapacity
storageclasses                    sc                                              storage.k8s.io/v1                      false        StorageClass
volumeattachments                                                                 storage.k8s.io/v1                      false        VolumeAttachment

```

- shortnames - 약어

mepsaced - true → namespace 존재 / false → cluster level 리소스

## 2) 클러스터의 주요 컴포넌트

```bash
kubectl get pods -n kube-system
```

```bash
[root@k8s-master kubernetes]# kubectl get pods -n kube-system
NAME                                 READY   STATUS    RESTARTS        AGE
coredns-5d78c9869d-7f92q             1/1     Running   1               4d
coredns-5d78c9869d-jxpkm             1/1     Running   1               4d
etcd-k8s-master                      1/1     Running   1 (3d15h ago)   4d
kube-apiserver-k8s-master            1/1     Running   1 (3d15h ago)   4d
kube-controller-manager-k8s-master   1/1     Running   1 (3d15h ago)   4d
kube-proxy-gmft2                     1/1     Running   1 (3d15h ago)   4d
kube-scheduler-k8s-master            1/1     Running   1 (3d15h ago)   4d
metrics-server-7db4fb59f9-4lj72      1/1     Running   1 (3d15h ago)   4d
```

### 로그 확인

```bash
kubectl logs -n kube-system etcd-k8s-master

kubectl logs -n kube-system kube-scheduler-k8s-master

kubectl logs -n kube-system kube-apiserver-k8s-master
```

## 3) 트러블 슈팅

### (1) kubelet 상태 확인

```bash
systemctl status kubelet
```

```bash
[root@k8s-master kubernetes]# systemctl status kubelet
● kubelet.service - kubelet: The Kubernetes Node Agent
   Loaded: loaded (/usr/lib/systemd/system/kubelet.service; enabled; vendor preset: disabled)
  Drop-In: /usr/lib/systemd/system/kubelet.service.d
           └─10-kubeadm.conf
   Active: active (running) since Tue 2024-09-10 08:34:05 KST; 3 days ago
     Docs: https://kubernetes.io/docs/
 Main PID: 992 (kubelet)
    Tasks: 17 (limit: 37660)
   Memory: 182.6M
   CGroup: /system.slice/kubelet.service
           └─992 /usr/bin/kubelet --bootstrap-kubeconfig=/etc/kubernetes/bootstrap-kubelet.conf --kubeconfig=>

Sep 10 18:25:13 k8s-master kubelet[992]: I0910 18:25:13.966456     992 operation_generator.go:661] "MountVolu>
Sep 10 18:25:15 k8s-master kubelet[992]: I0910 18:25:15.118550     992 kubelet_volumes.go:161] "Cleaned up or>
Sep 10 18:25:15 k8s-master kubelet[992]: I0910 18:25:15.118971     992 kubelet_volumes.go:161] "Cleaned up or>
Sep 10 18:25:15 k8s-master kubelet[992]: I0910 18:25:15.389651     992 pod_startup_latency_tracker.go:102] "O>
Sep 10 18:25:16 k8s-master kubelet[992]: I0910 18:25:16.397415     992 pod_startup_latency_tracker.go:102] "O>
Sep 10 18:25:44 k8s-master kubelet[992]: I0910 18:25:44.581652     992 scope.go:115] "RemoveContainer" contai>
Sep 10 18:29:54 k8s-master kubelet[992]: E0910 18:29:54.729545     992 kubelet.go:2431] "Housekeeping took lo>
Sep 10 18:34:24 k8s-master kubelet[992]: E0910 18:34:24.571680     992 kubelet.go:2431] "Housekeeping took lo>
Sep 13 23:55:39 k8s-master kubelet[992]: I0913 23:55:39.309288     992 trace.go:219] Trace[651070083]: "Calcu>
Sep 13 23:55:39 k8s-master kubelet[992]: Trace[651070083]: [1.088768492s] [1.088768492s] END
```

- running 등의 상태 확인
- 문제가 있는 경우 kubelet 재시작

    ```bash
    systemctl (restart or start) kubelet
    ```


### (2) kubelet 상세 로그 확인

```bash
journalctl -u kubelet | tail -10
```

```bash
[root@k8s-master kubernetes]# journalctl -u kubelet | tail -10
Sep 10 18:25:13 k8s-master kubelet[992]: I0910 18:25:13.966456     992 operation_generator.go:661] "MountVolume.MountDevice succeeded for volume \"api-tester-1231-files\" (UniqueName: \"kubernetes.io/local-volume/api-tester-1231-files\") pod \"api-tester-1231-6f898b9fcc-2wbhg\" (UID: \"dad594a8-7979-459a-b218-b788ef295735\") device mount path \"/root/k8s-local-volume/1231\"" pod="anotherclass-123/api-tester-1231-6f898b9fcc-2wbhg"
Sep 10 18:25:15 k8s-master kubelet[992]: I0910 18:25:15.118550     992 kubelet_volumes.go:161] "Cleaned up orphaned pod volumes dir" podUID=2ef3a0d5-3de1-4d46-9071-153f9b2179e4 path="/var/lib/kubelet/pods/2ef3a0d5-3de1-4d46-9071-153f9b2179e4/volumes"
Sep 10 18:25:15 k8s-master kubelet[992]: I0910 18:25:15.118971     992 kubelet_volumes.go:161] "Cleaned up orphaned pod volumes dir" podUID=f2abb4da-afd1-405c-b696-8412c9b79b00 path="/var/lib/kubelet/pods/f2abb4da-afd1-405c-b696-8412c9b79b00/volumes"
Sep 10 18:25:15 k8s-master kubelet[992]: I0910 18:25:15.389651     992 pod_startup_latency_tracker.go:102] "Observed pod startup duration" pod="anotherclass-123/api-tester-1231-6f898b9fcc-2wbhg" podStartSLOduration=2.389623303 podCreationTimestamp="2024-09-10 18:25:13 +0900 KST" firstStartedPulling="0001-01-01 00:00:00 +0000 UTC" lastFinishedPulling="0001-01-01 00:00:00 +0000 UTC" observedRunningTime="2024-09-10 18:25:15.389056831 +0900 KST m=+35465.672845879" watchObservedRunningTime="2024-09-10 18:25:15.389623303 +0900 KST m=+35465.673412339"
Sep 10 18:25:16 k8s-master kubelet[992]: I0910 18:25:16.397415     992 pod_startup_latency_tracker.go:102] "Observed pod startup duration" pod="anotherclass-123/api-tester-1231-6f898b9fcc-lbgjg" podStartSLOduration=3.397389965 podCreationTimestamp="2024-09-10 18:25:13 +0900 KST" firstStartedPulling="0001-01-01 00:00:00 +0000 UTC" lastFinishedPulling="0001-01-01 00:00:00 +0000 UTC" observedRunningTime="2024-09-10 18:25:16.397295575 +0900 KST m=+35466.681084623" watchObservedRunningTime="2024-09-10 18:25:16.397389965 +0900 KST m=+35466.681179002"
Sep 10 18:25:44 k8s-master kubelet[992]: I0910 18:25:44.581652     992 scope.go:115] "RemoveContainer" containerID="075f93c2bee729e391df939b5139d8bfb214071324387b6b91a4e1942f2442f1"
Sep 10 18:29:54 k8s-master kubelet[992]: E0910 18:29:54.729545     992 kubelet.go:2431] "Housekeeping took longer than expected" err="housekeeping took too long" expected="1s" actual="1.127s"
Sep 10 18:34:24 k8s-master kubelet[992]: E0910 18:34:24.571680     992 kubelet.go:2431] "Housekeeping took longer than expected" err="housekeeping took too long" expected="1s" actual="1.211s"
Sep 13 23:55:39 k8s-master kubelet[992]: I0913 23:55:39.309288     992 trace.go:219] Trace[651070083]: "Calculate volume metrics of kube-api-access-mjxmk for pod kubernetes-dashboard/kubernetes-dashboard-6bc7c98694-sgrkl" (13-Sep-2024 23:55:38.220) (total time: 1088ms):
Sep 13 23:55:39 k8s-master kubelet[992]: Trace[651070083]: [1.088768492s] [1.088768492s] END
```

- 로그를 확인해도 문제를 못찾는 경우, 한 두번 발생하는 문제는 VM을 재기동 하는 것이 나을 수 있음.
- 계속 발생한다면 Cluster를 재설치하거나 해결이 필요

### (3) containerd 상태 확인

```bash
systemctl status containerd
```

```bash
[root@k8s-master kubernetes]# systemctl status containerd
● containerd.service - containerd container runtime
   Loaded: loaded (/usr/lib/systemd/system/containerd.service; enabled; vendor preset: disabled)
   Active: active (running) since Tue 2024-09-10 08:34:03 KST; 3 days ago
     Docs: https://containerd.io
  Process: 692 ExecStartPre=/sbin/modprobe overlay (code=exited, status=0/SUCCESS)
 Main PID: 698 (containerd)
    Tasks: 392
   Memory: 670.3M
   CGroup: /system.slice/containerd.service
           ├─   698 /usr/bin/containerd
           ├─  1094 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 2279f09fcf4f05daf0fd65dcfc32f48fbb>
           ├─  1097 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 3faecc0188513eeb7bdbe3a3a12051ca1b>
           ├─  1099 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id b1200de346019cec1914b450e58e16e8a2>
           ├─  1105 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 738c17d221a9ee44a3e5399f4b3a11d2b0>
           ├─  1718 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 1572b331bb992fd40b7a1739a095726a62>
           ├─  1889 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id b9ca699dd2aec065aed5526e8ffc919272>
           ├─  1923 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 50cb1b6f44dede8672b12d07f5bb23fcfe>
           ├─  1971 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 7686f567079201976898156cedd75190b8>
           ├─  1978 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 362231669aaf0d0afbd2354eb91a24c77c>
           ├─  3219 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 573e779758b98dadfa1fef7f657025e7c8>
           ├─  3224 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 7bfce54a5922cccca5befea72d2ef445b7>
           ├─  3359 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 3a6a2856b2b27ea1e44a0d0c720564d016>
           ├─  3501 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 4e4abffab7bfe0a2104133bb503f31bf1e>
           ├─  3523 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id fcbaa8add4f936db8ee2bb9eb3282523cf>
           ├─  3747 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 1f89cd99ff08de392d981e1030e974d9cf>
           ├─  3804 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id c4531c9e3b801e7a290569a9cf96d57800>
           ├─  3851 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 455711e483655ecb0d4a70f3c10c40ae2e>
           ├─  3946 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id a704447c4af8b30e3f25a7cd1a144ac973>
           ├─  4012 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 646443df01f10e331f747573a9ca578027>
           ├─  4038 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 29f2147046fdd8a7777e3a5f72f1e0f726>
           ├─  4054 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 1f8ce8548ba1cc34712a2b7cab7f583b36>
           ├─  4216 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id c8bce1be284fa0f1577a090d3edff481ab>
           ├─  4336 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 5d76c5465a3f2eaf72de93a90622f898ad>
           ├─  4938 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 9fd7a64d171f27c3c7347d584a93720885>
           ├─  4999 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 3c367fa1d0c9a3d15fa5948503ec3d4b8e>
           ├─  5129 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 02e0549b4f3d4ec5f1d57581f6707141f5>
           ├─ 12532 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id ee7da128e104c2613e33f7b9d629ba2b79>
           ├─369240 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 04f86ffdf1ddc8b9ba0b574d9587f8f039>
           └─369290 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id cf738743058302e57998432923397c4a00>

Sep 10 18:25:45 k8s-master containerd[698]: time="2024-09-10T18:25:45.193891975+09:00" level=info msg="TearDo>
Sep 10 18:25:45 k8s-master containerd[698]: time="2024-09-10T18:25:45.206672279+09:00" level=info msg="Remove>
Sep 10 18:34:52 k8s-master containerd[698]: time="2024-09-10T18:34:52.126298093+09:00" level=info msg="Contai>
Sep 10 18:34:52 k8s-master containerd[698]: time="2024-09-10T18:34:52.127067352+09:00" level=error msg="Faile>
```

### (4) containerd 상세 로그 확인

```bash
journalctl -u containerd | tail -10
```

```bash
[root@k8s-master kubernetes]# journalctl -u containerd | tail -10
Sep 10 18:25:45 k8s-master containerd[698]: time="2024-09-10T18:25:45.193891975+09:00" level=info msg="TearDown network for sandbox \"006ee6248e6894784358049a0353df964bd42bff2a4e3233ad52fd328a30c7f5\" successfully"
Sep 10 18:25:45 k8s-master containerd[698]: time="2024-09-10T18:25:45.206672279+09:00" level=info msg="RemovePodSandbox \"006ee6248e6894784358049a0353df964bd42bff2a4e3233ad52fd328a30c7f5\" returns successfully"
Sep 10 18:34:52 k8s-master containerd[698]: time="2024-09-10T18:34:52.126298093+09:00" level=info msg="Container exec \"38767e8d9fe404162d8592e98aa61ce868bd7fc9f48e523057cdf32114e4a348\" stdin closed"
Sep 10 18:34:52 k8s-master containerd[698]: time="2024-09-10T18:34:52.127067352+09:00" level=error msg="Failed to pipe \"stdout\" for container exec \"38767e8d9fe404162d8592e98aa61ce868bd7fc9f48e523057cdf32114e4a348\"" error="read /proc/self/fd/417: file already closed"
Sep 10 18:34:52 k8s-master containerd[698]: time="2024-09-10T18:34:52.280016507+09:00" level=info msg="Container exec \"57589eb9d001c7ed3bd3613c47f5ad08291693554496141e9c7dd28695e91f9e\" stdin closed"
Sep 10 18:34:52 k8s-master containerd[698]: time="2024-09-10T18:34:52.280304315+09:00" level=error msg="Failed to pipe \"stdout\" for container exec \"57589eb9d001c7ed3bd3613c47f5ad08291693554496141e9c7dd28695e91f9e\"" error="read /proc/self/fd/417: file already closed"
Sep 10 18:37:09 k8s-master containerd[698]: time="2024-09-10T18:37:09.089620998+09:00" level=info msg="Container exec \"05a6af42795d1e27b7816e9112b897a49424e2761953963d95a3c6b21f298c8b\" stdin closed"
Sep 10 18:37:09 k8s-master containerd[698]: time="2024-09-10T18:37:09.089723404+09:00" level=error msg="Failed to pipe \"stdout\" for container exec \"05a6af42795d1e27b7816e9112b897a49424e2761953963d95a3c6b21f298c8b\"" error="read /proc/self/fd/417: file already closed"
Sep 10 18:37:11 k8s-master containerd[698]: time="2024-09-10T18:37:11.546734759+09:00" level=info msg="Container exec \"271bd082d0a8989c99bac38c2fe0cc04b8f0de523d5356f18ca2c1921e57bd12\" stdin closed"
Sep 10 18:37:11 k8s-master containerd[698]: time="2024-09-10T18:37:11.547832251+09:00" level=error msg="Failed to pipe \"stdout\" for container exec \"271bd082d0a8989c99bac38c2fe0cc04b8f0de523d5356f18ca2c1921e57bd12\"" error="read /proc/self/fd/417: file already closed"
```

### (5) 노드 상태 확인

```bash
kubectl get nodes -o wide
```

```bash
[root@k8s-master kubernetes]# kubectl get nodes -o wide
NAME         STATUS   ROLES           AGE   VERSION   INTERNAL-IP     EXTERNAL-IP   OS-IMAGE                           KERNEL-VERSION                 CONTAINER-RUNTIME
k8s-master   Ready    control-plane   4d    v1.27.2   192.168.56.30   <none>        Rocky Linux 8.8 (Green Obsidian)   4.18.0-477.10.1.el8_8.x86_64   containerd://1.6.21
```

### (6) 노드 상세 정보 확인

```bash
kubectl describe node k8s-master
```

```bash
[root@k8s-master kubernetes]# kubectl describe node k8s-master
Name:               k8s-master
Roles:              control-plane
Labels:             beta.kubernetes.io/arch=amd64
                    beta.kubernetes.io/os=linux
                    kubernetes.io/arch=amd64
                    kubernetes.io/hostname=k8s-master
                    kubernetes.io/os=linux
                    node-role.kubernetes.io/control-plane=
                    node.kubernetes.io/exclude-from-external-load-balancers=
Annotations:        csi.volume.kubernetes.io/nodeid: {"csi.tigera.io":"k8s-master"}
                    kubeadm.alpha.kubernetes.io/cri-socket: unix:///var/run/containerd/containerd.sock
                    node.alpha.kubernetes.io/ttl: 0
                    projectcalico.org/IPv4Address: 192.168.56.30/24
                    projectcalico.org/IPv4VXLANTunnelAddr: 20.96.235.192
                    volumes.kubernetes.io/controller-managed-attach-detach: true
CreationTimestamp:  Mon, 09 Sep 2024 23:58:34 +0900
Taints:             <none>
Unschedulable:      false
Lease:
  HolderIdentity:  k8s-master
  AcquireTime:     <unset>
  RenewTime:       Sat, 14 Sep 2024 00:41:47 +0900
Conditions:
  Type                 Status  LastHeartbeatTime                 LastTransitionTime                Reason                       Message
  ----                 ------  -----------------                 ------------------                ------                       -------
  NetworkUnavailable   False   Tue, 10 Sep 2024 08:35:28 +0900   Tue, 10 Sep 2024 08:35:28 +0900   CalicoIsUp                   Calico is running on this node
  MemoryPressure       False   Sat, 14 Sep 2024 00:39:40 +0900   Mon, 09 Sep 2024 23:58:34 +0900   KubeletHasSufficientMemory   kubelet has sufficient memory available
  DiskPressure         False   Sat, 14 Sep 2024 00:39:40 +0900   Mon, 09 Sep 2024 23:58:34 +0900   KubeletHasNoDiskPressure     kubelet has no disk pressure
  PIDPressure          False   Sat, 14 Sep 2024 00:39:40 +0900   Mon, 09 Sep 2024 23:58:34 +0900   KubeletHasSufficientPID      kubelet has sufficient PID available
  Ready                True    Sat, 14 Sep 2024 00:39:40 +0900   Mon, 09 Sep 2024 23:59:41 +0900   KubeletReady                 kubelet is posting ready status
Addresses:
  InternalIP:  192.168.56.30
  Hostname:    k8s-master
Capacity:
  cpu:                4
  ephemeral-storage:  51290092Ki
  hugepages-2Mi:      0
  memory:             6063276Ki
  pods:               110
Allocatable:
  cpu:                4
  ephemeral-storage:  47268948709
  hugepages-2Mi:      0
  memory:             5960876Ki
  pods:               110
System Info:
  Machine ID:                 c3a17dea0a4c41c5a84d5d7004316df1
  System UUID:                98948b72-5dfc-1046-a105-2c1cda8756b6
  Boot ID:                    1222d5e7-75ff-43fb-a6fd-1b9508ea4612
  Kernel Version:             4.18.0-477.10.1.el8_8.x86_64
  OS Image:                   Rocky Linux 8.8 (Green Obsidian)
  Operating System:           linux
  Architecture:               amd64
  Container Runtime Version:  containerd://1.6.21
  Kubelet Version:            v1.27.2
  Kube-Proxy Version:         v1.27.2
PodCIDR:                      20.96.0.0/24
PodCIDRs:                     20.96.0.0/24
Non-terminated Pods:          (29 in total)
  Namespace                   Name                                          CPU Requests  CPU Limits  Memory Requests  Memory Limits  Age
  ---------                   ----                                          ------------  ----------  ---------------  -------------  ---
  anotherclass-123            api-tester-1231-6f898b9fcc-2wbhg              100m (2%)     200m (5%)   100Mi (1%)       200Mi (3%)     3d6h
  anotherclass-123            api-tester-1231-6f898b9fcc-lbgjg              100m (2%)     200m (5%)   100Mi (1%)       200Mi (3%)     3d6h
  calico-apiserver            calico-apiserver-7775b667cd-jk55b             0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  calico-apiserver            calico-apiserver-7775b667cd-mqjwp             0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  calico-system               calico-kube-controllers-5f55f4cc98-ncg4f      0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  calico-system               calico-node-nq9sg                             0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  calico-system               calico-typha-74bf546b6c-xdkg7                 0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  calico-system               csi-node-driver-5wh5w                         0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  default                     app-1-2-2-1-78cbbff668-8s94x                  100m (2%)     200m (5%)   100Mi (1%)       200Mi (3%)     3d15h
  default                     app-1-2-2-1-78cbbff668-vzhll                  100m (2%)     200m (5%)   100Mi (1%)       200Mi (3%)     4d
  kube-system                 coredns-5d78c9869d-7f92q                      100m (2%)     0 (0%)      70Mi (1%)        170Mi (2%)     4d
  kube-system                 coredns-5d78c9869d-jxpkm                      100m (2%)     0 (0%)      70Mi (1%)        170Mi (2%)     4d
  kube-system                 etcd-k8s-master                               100m (2%)     0 (0%)      100Mi (1%)       0 (0%)         4d
  kube-system                 kube-apiserver-k8s-master                     250m (6%)     0 (0%)      0 (0%)           0 (0%)         4d
  kube-system                 kube-controller-manager-k8s-master            200m (5%)     0 (0%)      0 (0%)           0 (0%)         4d
  kube-system                 kube-proxy-gmft2                              0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  kube-system                 kube-scheduler-k8s-master                     100m (2%)     0 (0%)      0 (0%)           0 (0%)         4d
  kube-system                 metrics-server-7db4fb59f9-4lj72               100m (2%)     0 (0%)      200Mi (3%)       0 (0%)         4d
  kubernetes-dashboard        dashboard-metrics-scraper-5cb4f4bb9c-rm6gs    0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  kubernetes-dashboard        kubernetes-dashboard-6bc7c98694-sgrkl         0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  loki-stack                  loki-stack-0                                  0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  loki-stack                  loki-stack-promtail-zfwz5                     0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
  monitoring                  grafana-646b5d5dd8-rrgdz                      100m (2%)     200m (5%)   100Mi (1%)       200Mi (3%)     4d
  monitoring                  kube-state-metrics-86c66b4fcd-pstzj           40m (1%)      160m (4%)   230Mi (3%)       330Mi (5%)     4d
  monitoring                  node-exporter-bm2pn                           112m (2%)     270m (6%)   200Mi (3%)       220Mi (3%)     4d
  monitoring                  prometheus-adapter-648959cd84-bhhc4           102m (2%)     250m (6%)   180Mi (3%)       180Mi (3%)     4d
  monitoring                  prometheus-k8s-0                              100m (2%)     100m (2%)   450Mi (7%)       50Mi (0%)      4d
  monitoring                  prometheus-operator-7ff88bdb95-ps9dj          110m (2%)     220m (5%)   120Mi (2%)       240Mi (4%)     4d
  tigera-operator             tigera-operator-84cf9b6dbb-nlggh              0 (0%)        0 (0%)      0 (0%)           0 (0%)         4d
Allocated resources:
  (Total limits may be over 100 percent, i.e., overcommitted.)
  Resource           Requests      Limits
  --------           --------      ------
  cpu                1914m (47%)   2 (50%)
  memory             2120Mi (36%)  2360Mi (40%)
  ephemeral-storage  0 (0%)        0 (0%)
  hugepages-2Mi      0 (0%)        0 (0%)
Events:              <none>

```

### (7) Pod 상태 확인

```bash
kubectl get pods -A -o wide
```

```bash
[root@k8s-master kubernetes]# kubectl get pods -A -o wide
NAMESPACE              NAME                                         READY   STATUS    RESTARTS        AGE     IP              NODE         NOMINATED NODE   READINESS GATES
anotherclass-123       api-tester-1231-6f898b9fcc-2wbhg             1/1     Running   0               3d7h    20.96.235.250   k8s-master   <none>           <none>
anotherclass-123       api-tester-1231-6f898b9fcc-lbgjg             1/1     Running   0               3d7h    20.96.235.251   k8s-master   <none>           <none>
calico-apiserver       calico-apiserver-7775b667cd-jk55b            1/1     Running   1 (3d17h ago)   4d1h    20.96.235.220   k8s-master   <none>           <none>
calico-apiserver       calico-apiserver-7775b667cd-mqjwp            1/1     Running   1 (3d17h ago)   4d1h    20.96.235.214   k8s-master   <none>           <none>
calico-system          calico-kube-controllers-5f55f4cc98-ncg4f     1/1     Running   1 (3d17h ago)   4d1h    20.96.235.219   k8s-master   <none>           <none>
calico-system          calico-node-nq9sg                            1/1     Running   1 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>
calico-system          calico-typha-74bf546b6c-xdkg7                1/1     Running   1 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>
calico-system          csi-node-driver-5wh5w                        2/2     Running   2 (3d17h ago)   4d1h    20.96.235.228   k8s-master   <none>           <none>
default                app-1-2-2-1-78cbbff668-8s94x                 1/1     Running   0               3d17h   20.96.235.231   k8s-master   <none>           <none>
default                app-1-2-2-1-78cbbff668-vzhll                 1/1     Running   1 (3d17h ago)   4d1h    20.96.235.221   k8s-master   <none>           <none>
kube-system            coredns-5d78c9869d-7f92q                     1/1     Running   1               4d1h    20.96.235.225   k8s-master   <none>           <none>
kube-system            coredns-5d78c9869d-jxpkm                     1/1     Running   1               4d1h    20.96.235.222   k8s-master   <none>           <none>
kube-system            etcd-k8s-master                              1/1     Running   1 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>
kube-system            kube-apiserver-k8s-master                    1/1     Running   1 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>
kube-system            kube-controller-manager-k8s-master           1/1     Running   1 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>
kube-system            kube-proxy-gmft2                             1/1     Running   1 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>
kube-system            kube-scheduler-k8s-master                    1/1     Running   1 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>
kube-system            metrics-server-7db4fb59f9-4lj72              1/1     Running   1 (3d17h ago)   4d1h    20.96.235.212   k8s-master   <none>           <none>
kubernetes-dashboard   dashboard-metrics-scraper-5cb4f4bb9c-rm6gs   1/1     Running   1               4d1h    20.96.235.224   k8s-master   <none>           <none>
kubernetes-dashboard   kubernetes-dashboard-6bc7c98694-sgrkl        1/1     Running   1 (3d17h ago)   4d1h    20.96.235.211   k8s-master   <none>           <none>
loki-stack             loki-stack-0                                 1/1     Running   1 (3d17h ago)   4d1h    20.96.235.215   k8s-master   <none>           <none>
loki-stack             loki-stack-promtail-zfwz5                    1/1     Running   1               4d1h    20.96.235.223   k8s-master   <none>           <none>
monitoring             grafana-646b5d5dd8-rrgdz                     1/1     Running   1 (3d17h ago)   4d1h    20.96.235.218   k8s-master   <none>           <none>
monitoring             kube-state-metrics-86c66b4fcd-pstzj          3/3     Running   3 (3d17h ago)   4d1h    20.96.235.226   k8s-master   <none>           <none>
monitoring             node-exporter-bm2pn                          2/2     Running   2 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>
monitoring             prometheus-adapter-648959cd84-bhhc4          1/1     Running   1 (3d17h ago)   4d1h    20.96.235.213   k8s-master   <none>           <none>
monitoring             prometheus-k8s-0                             2/2     Running   2 (3d17h ago)   4d1h    20.96.235.217   k8s-master   <none>           <none>
monitoring             prometheus-operator-7ff88bdb95-ps9dj         2/2     Running   2 (3d17h ago)   4d1h    20.96.235.227   k8s-master   <none>           <none>
tigera-operator        tigera-operator-84cf9b6dbb-nlggh             1/1     Running   2 (3d17h ago)   4d1h    192.168.56.30   k8s-master   <none>           <none>

```

### (8) Pod 이벤트 확인 (기본값: 1h)

```bash
kubectl get events -A
```

```bash
kubectl events -n anotherclass-123 --types=Warning  (or Normal)
```

- 만약 이벤트가 없으면 리소스 문제라기 보다는 App이 기동하면서 생기는 문제일 수 있음

### (9) Pod의 Log 확인

```bash
kubectl logs -n anotherclass-123 <pod-name> --tail 10    // 10줄 만 조회하기

kubectl logs -n anotherclass-123 <pod-name> -f           // 실시간으로 조회 걸어 놓기

kubectl logs -n anotherclass-123 <pod-name> --since=1m   // 1분 이내에 생성된 로그만 보기
```

### (10) iptables의 매핑 정보 확인

```bash
iptables -t nat -L KUBE-NODEPORTS -n  | column -t
```

```bash
[root@k8s-master kubernetes]# iptables -t nat -L KUBE-NODEPORTS -n  | column -t
Chain                      KUBE-NODEPORTS  (1   references)                                                   
target                     prot            opt  source       destination                                      
KUBE-EXT-D56K3FJFRDOXXBVS  tcp             --   0.0.0.0/0    0.0.0.0/0    /*  default/app-1-2-2-1                        */
KUBE-EXT-AWA2CQSXVI7X2GE5  tcp             --   0.0.0.0/0    0.0.0.0/0    /*  monitoring/grafana:http                    */
KUBE-EXT-IFE4WDFHTFWLXCFL  tcp             --   0.0.0.0/0    0.0.0.0/0    /*  anotherclass-123/api-tester-1231           */
KUBE-EXT-CEZPIJSAUFW5MYPQ  tcp             --   0.0.0.0/0    0.0.0.0/0    /*  kubernetes-dashboard/kubernetes-dashboard  */
```