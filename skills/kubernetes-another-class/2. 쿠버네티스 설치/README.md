# 2. 쿠버네티스 설치

---

<img src="https://github.com/user-attachments/assets/abd4c2bd-1d82-4db9-859d-208abc2e7695" alt="adder" width="100%" />

## 1) Host OS에 Virtual Box VMWare 설치하기

## 2) VM 위에 Vagrant 설치

### Vagrant (베이그란트)

- 가상화 환경을 관리하고 프로비저닝하는 도구. (naver cloud의 init script와 비슷하구나)

## 3) Vagrant 스크립트로 Rocky Linux & Kubernetes 설치

### 스크립트 다운로드 및 실행

```
# Vagrant 폴더 생성
C:\Users\사용자> mkdir k8s && cd k8s

# Vagrant 스크립트 다운로드
curl -O https://raw.githubusercontent.com/k8s-1pro/install/main/ground/k8s-1.27/vagrant-2.3.4/Vagrantfile

# Rocky Linux Repo 세팅
curl -O https://raw.githubusercontent.com/k8s-1pro/install/main/ground/k8s-1.27/vagrant-2.3.4/rockylinux-repo.json
vagrant box add rockylinux-repo.json

# Vagrant Disk 설정 Plugin 설치 
vagrant plugin install vagrant-vbguest vagrant-disksize

# Vagrant 실행 (VM생성)
vagrant up
```

### 스크립트

```java

Vagrant.configure("2") do |config|
    
  config.vm.box = "rockylinux/8"
  # Disk 확장설정 추가
  config.disksize.size = "50GB"

  #https://cafe.naver.com/kubeops/26
  config.vbguest.auto_update = false
  config.vm.synced_folder "./", "/vagrant", disabled: true

  config.vm.provision :shell, privileged: true, inline: $install_default
  config.vm.define "master-node" do |master|
    master.vm.hostname = "k8s-master"
    master.vm.network "private_network", ip: "192.168.56.30"
	master.vm.provider :virtualbox do |vb|
      vb.memory = 6144
      vb.cpus = 4
	  vb.customize ["modifyvm", :id, "--firmware", "efi"]
	end
    master.vm.provision :shell, privileged: true, inline: $install_master
  end

end

$install_default = <<-SHELL

echo '======== [4] Rocky Linux 기본 설정 ========'
echo '======== [4-1] 패키지 업데이트 ========'
# 강의와 동일한 실습 환경을 유지하기 위해 Linux Update 주석 처리
# yum -y update

echo '======== [4-2] 타임존 설정 ========'
timedatectl set-timezone Asia/Seoul

echo '======== [4-3] Disk 확장 / Bug: soft lockup 설정 추가========'
# https://cafe.naver.com/kubeops/25
yum install -y cloud-utils-growpart
growpart /dev/sda 4
xfs_growfs /dev/sda4
echo 0 > /proc/sys/kernel/hung_task_timeout_secs
echo "kernel.watchdog_thresh = 20" >> /etc/sysctl.conf

echo '======== [4-4] [WARNING FileExisting-tc]: tc not found in system path 로그 관련 업데이트 ========'
yum install -y yum-utils iproute-tc

echo '======= [4-4] hosts 설정 =========='
cat << EOF >> /etc/hosts
192.168.56.30 k8s-master
EOF

echo '======== [5] kubeadm 설치 전 사전작업 ========'
echo '======== [5] 방화벽 해제 ========'
systemctl stop firewalld && systemctl disable firewalld

echo '======== [5] Swap 비활성화 ========'
swapoff -a && sed -i '/ swap / s/^/#/' /etc/fstab

echo '======== [6] 컨테이너 런타임 설치 ========'
echo '======== [6-1] 컨테이너 런타임 설치 전 사전작업 ========'
echo '======== [6-1] iptable 세팅 ========'
cat <<EOF |tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

modprobe overlay
modprobe br_netfilter

cat <<EOF |tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sysctl --system

echo '======== [6-2] 컨테이너 런타임 (containerd 설치) ========'
echo '======== [6-2-1] containerd 패키지 설치 (option2) ========'
echo '======== [6-2-1-1] docker engine 설치 ========'
echo '======== [6-2-1-1] repo 설정 ========'
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

echo '======== [6-2-1-1] containerd 설치 ========'
yum install -y containerd.io-1.6.21-3.1.el8
systemctl daemon-reload
systemctl enable --now containerd

echo '======== [6-3] 컨테이너 런타임 : cri 활성화 ========'
# defualt cgroupfs에서 systemd로 변경 (kubernetes default는 systemd)
containerd config default > /etc/containerd/config.toml
sed -i 's/ SystemdCgroup = false/ SystemdCgroup = true/' /etc/containerd/config.toml
systemctl restart containerd

echo '======== [7] kubeadm 설치 ========'
echo '======== [7] repo 설정 ========'
cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://pkgs.k8s.io/core:/stable:/v1.27/rpm/
enabled=1
gpgcheck=1
gpgkey=https://pkgs.k8s.io/core:/stable:/v1.27/rpm/repodata/repomd.xml.key
exclude=kubelet kubeadm kubectl cri-tools kubernetes-cni
EOF

echo '======== [7] SELinux 설정 ========'
setenforce 0
sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config

echo '======== [7] kubelet, kubeadm, kubectl 패키지 설치 ========'
yum install -y kubelet-1.27.2-150500.1.1.x86_64 kubeadm-1.27.2-150500.1.1.x86_64 kubectl-1.27.2-150500.1.1.x86_64 --disableexcludes=kubernetes
systemctl enable --now kubelet

SHELL

$install_master = <<-SHELL

echo '======== [8] kubeadm으로 클러스터 생성  ========'
echo '======== [8-1] 클러스터 초기화 (Pod Network 세팅) ========'
kubeadm init --pod-network-cidr=20.96.0.0/16 --apiserver-advertise-address 192.168.56.30

echo '======== [8-2] kubectl 사용 설정 ========'
mkdir -p $HOME/.kube
cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
chown $(id -u):$(id -g) $HOME/.kube/config

echo '======== [8-3] Pod Network 설치 (calico) ========'
kubectl create -f https://raw.githubusercontent.com/k8s-1pro/install/main/ground/k8s-1.27/calico-3.26.4/calico.yaml
kubectl create -f https://raw.githubusercontent.com/k8s-1pro/install/main/ground/k8s-1.27/calico-3.26.4/calico-custom.yaml

echo '======== [8-4] Master에 Pod를 생성 할수 있도록 설정 ========'
kubectl taint nodes k8s-master node-role.kubernetes.io/control-plane-

echo '======== [9] 쿠버네티스 편의기능 설치 ========'
echo '======== [9-1] kubectl 자동완성 기능 ========'
echo "source <(kubectl completion bash)" >> ~/.bashrc
echo 'alias k=kubectl' >>~/.bashrc
echo 'complete -o default -F __start_kubectl k' >>~/.bashrc

echo '======== [9-2] Dashboard 설치 ========'
kubectl create -f https://raw.githubusercontent.com/k8s-1pro/install/main/ground/k8s-1.27/dashboard-2.7.0/dashboard.yaml

echo '======== [9-3] Metrics Server 설치 ========'
kubectl create -f https://raw.githubusercontent.com/k8s-1pro/install/main/ground/k8s-1.27/metrics-server-0.6.3/metrics-server.yaml
SHELL
```

### Kubernetes 설치 시 유의사항

[시작하기](https://kubernetes.io/ko/docs/setup/)

공식 문서를 잘 참고하는 것이 좋다.

- 워커노드는 세팅이 동일하므로 VM 복사 기능을 사용해서 구성하는 경우가 많은데, 이 때 VM의 ‘고유속성’까지 복사하여 사용하면 충돌이 나서 안되니까 주의
- 마스터노드 세팅 시 `포트 개방` 및 `스왑 비활성화`를 꼭 필요로 함

### Vagrant 스크립트를 사용한 설치 흐름

<img src="https://github.com/user-attachments/assets/2e9aba7f-231b-49c4-8c67-9038327240c4" alt="adder" width="100%" />

1. MasterNode의 이름, Network의 이름과 ip를 할당한다. (port는 기본 22번 포트)
    - 명시적으로 만들어주는 Network는 Host에서만 접근이 가능한 Host-Only로 만들어지며, 이 ip는 Host가 사용하는 공유기(NAT)의 사설 ip 할당 범위와 대역폭이 겹치지 않아야 한다. (그래야 충돌이 나지 않음)
    - 명시적으로 만들어주는 Network 외에 Vagrant가 만들어주는 Network가 있는데, 이게 `NAT` 이다.
    - NAT 네트워크는 내 VM을 외부와 연결시켜주는 역할을 한다.
2. VM 위에 올라가는 Linux를 설치한다.
    - 이름, ip, Host pc의 자원을 할당한다.
3. Containerd와 Kubernetes 패키지를 설치한다.

<img src="https://github.com/user-attachments/assets/2c8770fe-3e2a-4145-ada4-02167696a378" alt="adder" width="100%" />

<img src="https://github.com/user-attachments/assets/0781a5ba-88b4-4602-8081-7033d5fd5177" alt="adder" width="100%" />

## 4) Linux 기본 설정 세팅

- 기본 패키지 업데이트, 타임존 설정 등이 이루어진다.

## 5) kubeadm 설치 전 사전작업

- 방화벽 설정, 스왑 메모리 체크(비활성화) 등이 이루어진다.

## 6) 컨테이너 런타임 설치

- 컨테이너 런타임 설치 전 iptables 세팅
    - 공유기의 NAT table이랑 비슷한 것으로 보임. Linux에서 할당받은 ip + port가 Kubernetes Cluster 환경 안에서 어떤 pod랑 연결되어 있는지 확인하는 용도로 보임.
- 컨테이너 런타임 (containerd) 설치

  > [중요1]
  > 
  > kernel level의 격리 기술 중 `cgroup`이라는게 있는데, 이 종류가 `systemd`와 `cgroupfs` 두 가지임.
  > 이 `cgroup` 종류가 `kubernetes`와 `containderd`가 동일해야함.
  > (기본값은 `cgroupfs`라서 아무 설정도 안하면 이렇게 다 맞춰져있음.

  > [중요2]
  > 
  > `containderd`설치 시 `kubernetes`와 버전이 잘 호환되는 것을 꼭 봐야함.
  > 추가로 `containderd`에서 `LTS` 버전인 `1.6.x` 버전을 설치하면 된다.

- 컨테이너 런타임 CRI(Container Runtime Interface) 활성화

## 7) kubeadm 설치

실행 가능한 최소 클러스터를 시작하고 실행하는 데 필요한 작업을 수행하는 도구이다.

- repo 설정
- SELinux 설정
- kubelet, kubeadm, kubectl 패키지 설치

## 8) Master-Node 세팅

- 클러스터 초기화
    - 필요한 패키지들이 설치가 됨
    - Pod Network 세팅
        - cidr을 이용해 pod network 대역폭을 지정할 수 있음. 이 범위로 pod의 network ip가 지정됨.
    - kubernetes 설치가 끝나면 kubernetes에 접속이 가능하도록 하는 `인증서`가 설치되어 있음
- kubectl 사용 설정
    - 설치된 `인증서`를 kubectl이 사용할 수 있도록 하는 세팅
- CNI(Container Network Interface) Plugin 설치 (calico - `컨테이너들간의 통신`을 관리하는 nework 솔루션)
- Master 에 Pod를 생성할 수 있도록 설정
    - 이 부분은 현재 실습 시에는 Master Node만 만들거라서 세팅
    - 실무에서는 Matser Node에는 유저가 만든 Pod는 올리지 않는 것이 좋

## 9) 편의기능 설치

- kubectl 자동완성 기능
- Dashboard 설치
- Metrics Server 설치

---

# 실습

## Virtual Box 실행

<img src="https://github.com/user-attachments/assets/4ddc95dc-3d17-412b-bc5c-8a017441e53f" alt="adder" width="60%" />

표시 부분에 실행을 해주어야 실행 중으로 변함

## Rocky Linux 버전 확인

```bash

[root@k8s-master ~]# cat /etc/*-release
Rocky Linux release 8.8 (Green Obsidian)
NAME="Rocky Linux"
VERSION="8.8 (Green Obsidian)"
ID="rocky"
ID_LIKE="rhel centos fedora"
VERSION_ID="8.8"
PLATFORM_ID="platform:el8"
PRETTY_NAME="Rocky Linux 8.8 (Green Obsidian)"
ANSI_COLOR="0;32"
LOGO="fedora-logo-icon"
CPE_NAME="cpe:/o:rocky:rocky:8:GA"
HOME_URL="https://rockylinux.org/"
BUG_REPORT_URL="https://bugs.rockylinux.org/"
SUPPORT_END="2029-05-31"
ROCKY_SUPPORT_PRODUCT="Rocky-Linux-8"
ROCKY_SUPPORT_PRODUCT_VERSION="8.8"
REDHAT_SUPPORT_PRODUCT="Rocky Linux"
REDHAT_SUPPORT_PRODUCT_VERSION="8.8"
Rocky Linux release 8.8 (Green Obsidian)
Rocky Linux release 8.8 (Green Obsidian)
Rocky Linux release 8.8 (Green Obsidian)
```

## Host 정보 확인

### Hostname

```bash
[root@k8s-master ~]# hostname
k8s-master
```

### Network 확인

```bash
[root@k8s-master ~]# ip addr
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 08:00:27:fc:e9:96 brd ff:ff:ff:ff:ff:ff
    altname enp0s3
    inet 10.0.2.15/24 brd 10.0.2.255 scope global dynamic noprefixroute eth0
       valid_lft 78435sec preferred_lft 78435sec
    inet6 fe80::a00:27ff:fefc:e996/64 scope link
       valid_lft forever preferred_lft forever
3: eth1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 08:00:27:42:c1:b6 brd ff:ff:ff:ff:ff:ff
    altname enp0s8
    inet 192.168.56.30/24 brd 192.168.56.255 scope global noprefixroute eth1
       valid_lft forever preferred_lft forever
    inet6 fe80::a00:27ff:fe42:c1b6/64 scope link
       valid_lft forever preferred_lft forever
...(후략)...
```

### 자원 확인

- cpu

    ```bash
    [root@k8s-master ~]# lscpu
    Architecture:        x86_64
    CPU op-mode(s):      32-bit, 64-bit
    Byte Order:          Little Endian
    CPU(s):              4
    On-line CPU(s) list: 0-3
    Thread(s) per core:  1
    Core(s) per socket:  4
    Socket(s):           1
    NUMA node(s):        1
    Vendor ID:           AuthenticAMD
    CPU family:          25
    Model:               97
    Model name:          AMD Ryzen 9 7900 12-Core Processor
    Stepping:            2
    CPU MHz:             3699.928
    BogoMIPS:            7399.85
    Hypervisor vendor:   KVM
    Virtualization type: full
    L1d cache:           32K
    L1i cache:           32K
    L2 cache:            1024K
    L3 cache:            65536K
    NUMA node0 CPU(s):   0-3
    Flags:               fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush mmx fxsr sse sse2 ht syscall nx mmxext fxsr_opt rdtscp lm constant_tsc rep_good nopl nonstop_tsc cpuid extd_apicid tsc_known_freq pni pclmulqdq ssse3 cx16 sse4_1 sse4_2 movbe popcnt aes rdrand hypervisor lahf_lm cmp_legacy cr8_legacy abm sse4a misalignsse 3dnowprefetch vmmcall fsgsbase bmi1 bmi2 invpcid rdseed clflushopt arat
    ```

- memory

    ```bash
    [root@k8s-master ~]# free -h
                  total        used        free      shared  buff/cache   available
    Mem:          5.8Gi       1.6Gi       422Mi        43Mi       3.7Gi       3.8Gi
    Swap:            0B          0B          0B   <-- swap 없음 확인
    
    ```


### timezone 확인

```bash
[root@k8s-master ~]# timedatectl
               Local time: Thu 2024-09-05 01:00:00 KST
           Universal time: Wed 2024-09-04 16:00:00 UTC
                 RTC time: Wed 2024-09-04 15:44:55
                Time zone: Asia/Seoul (KST, +0900)
System clock synchronized: no
              NTP service: active
          RTC in local TZ: no
```

시간이 좀 안맞음…

[시간 동기화 설정 점검 (Linux)](https://guide.ncloud-docs.com/docs/compute-vpc-ntp-check)

동기화 완료

```bash
[root@k8s-master ~]# timedatectl
               Local time: Sat 2024-09-07 00:11:06 KST
           Universal time: Fri 2024-09-06 15:11:06 UTC
                 RTC time: Fri 2024-09-06 15:11:08
                Time zone: Asia/Seoul (KST, +0900)
System clock synchronized: yes
              NTP service: active
          RTC in local TZ: no

```

### containerd 설치 및 실행 확인

```bash
[root@k8s-master ~]# systemctl status containerd
● containerd.service - containerd container runtime
   Loaded: loaded (/usr/lib/systemd/system/containerd.service; enabled; vendor preset: disabled)
   Active: active (running) since Wed 2024-09-04 22:45:04 KST; 2 days ago
     Docs: https://containerd.io
 Main PID: 25324 (containerd)
    Tasks: 231
   Memory: 2.7G
   CGroup: /system.slice/containerd.service
           ├─25324 /usr/bin/containerd
           ├─26454 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id c09ba970bffe4f271ca0b97f4608a7a715>
           ├─26481 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 7637fc61153749049b5420350af733c2f9>
           ├─26488 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 473995d7a68b9fbc2755a96ff701204d18>
           ├─26515 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 3df68a4c2a047c8b1f08d0f536172bca52>
           ├─26830 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 75e78458d83228f611f9e6865014914789>
           ├─26885 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id b1ecdfbbc7768999d82f09add75e94aac8>
           ├─27238 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id bd070601e680431608dae344922e9a85f1>
           ├─27279 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 7ba23d35b10efd1476d541c8958b83b9d3>
           ├─29147 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 4c808d67a176bef4a41714c0dba6a9628d>
           ├─29299 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 1997e88c40590fe6df27fb81d222fee6f4>
           ├─29479 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 27a029b7094c6ceb25a87ea84ff0733a58>
           ├─29502 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 04edabbb8aa8abc480f6e223931d1decff>
           ├─29721 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id eae409b19465310d2ded86a37f769313cd>
           ├─29762 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 71ca8d8101767c40d3022ffac0673ab8aa>
           ├─29888 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id cde9102235756e28647a87d141f580999f>
           ├─30268 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 335184f0e4cd365d6eefc43805826c56be>
           └─30284 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 8ff271324c27b4e636d8eee103ed3f9536>
```

### containerd cgroup 설정 확인

```bash
[root@k8s-master ~]# cat /etc/containerd/config.toml

... (중략) ...

          [plugins."io.containerd.grpc.v1.cri".containerd.runtimes.runc.options]
            BinaryName = ""
            CriuImagePath = ""
            CriuPath = ""
            CriuWorkPath = ""
            IoGid = 0
            IoUid = 0
            NoNewKeyring = false
            NoPivotRoot = false
            Root = ""
            ShimCgroup = ""
            SystemdCgroup = true  <---  이 부분이 false이면 cgroupfs(기본값)임

```

### kubelet cgroup 확인

```bash
[root@k8s-master ~]# kubectl get -n kube-system cm kubelet-config -o yaml
apiVersion: v1
data:
  kubelet: |
    apiVersion: kubelet.config.k8s.io/v1beta1
    authentication:
      anonymous:
        enabled: false
      webhook:
        cacheTTL: 0s
        enabled: true
      x509:
        clientCAFile: /etc/kubernetes/pki/ca.crt
    authorization:
      mode: Webhook
      webhook:
        cacheAuthorizedTTL: 0s
        cacheUnauthorizedTTL: 0s
    cgroupDriver: systemd      <-------------------- systemd 적용 되어 있음
    clusterDNS:
    - 10.96.0.10
    clusterDomain: cluster.local
    containerRuntimeEndpoint: ""
    cpuManagerReconcilePeriod: 0s
    evictionPressureTransitionPeriod: 0s
    fileCheckFrequency: 0s
    healthzBindAddress: 127.0.0.1
    healthzPort: 10248
    httpCheckFrequency: 0s
    imageMinimumGCAge: 0s
    kind: KubeletConfiguration
    logging:
      flushFrequency: 0
      options:
        json:
          infoBufferSize: "0"
      verbosity: 0
    memorySwap: {}
    nodeStatusReportFrequency: 0s
    nodeStatusUpdateFrequency: 0s
    rotateCertificates: true
    runtimeRequestTimeout: 0s
    shutdownGracePeriod: 0s
    shutdownGracePeriodCriticalPods: 0s
    staticPodPath: /etc/kubernetes/manifests
    streamingConnectionIdleTimeout: 0s
    syncFrequency: 0s
    volumeStatsAggPeriod: 0s
kind: ConfigMap
metadata:
  annotations:
    kubeadm.kubernetes.io/component-config.hash: sha256:1a57e3f87b04a6bde4ecbbdc2a102ed31a3de10425da3154f44eaf02012235cc
  creationTimestamp: "2024-09-04T13:47:15Z"
  name: kubelet-config
  namespace: kube-system
  resourceVersion: "241"
  uid: d3acbe5c-cfba-4066-a260-c75eaba0c11e

```

### 쿠버네티스 클러스터 상태 확인

```bash
[root@k8s-master ~]# kubectl get node
NAME         STATUS   ROLES           AGE    VERSION
k8s-master   Ready    control-plane   2d1h   v1.27.2

[root@k8s-master ~]# kubectl cluster-info
Kubernetes control plane is running at https://192.168.56.30:6443
CoreDNS is running at https://192.168.56.30:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.

[root@k8s-master ~]# kubectl get pods -n kube-system
NAME                                 READY   STATUS    RESTARTS      AGE
coredns-5d78c9869d-7vsvz             1/1     Running   0             2d1h
coredns-5d78c9869d-dmp4m             1/1     Running   0             2d1h
etcd-k8s-master                      1/1     Running   0             2d1h
kube-apiserver-k8s-master            1/1     Running   0             2d1h
kube-controller-manager-k8s-master   1/1     Running   1 (2d ago)    2d1h
kube-proxy-lw5lc                     1/1     Running   0             2d1h
kube-scheduler-k8s-master            1/1     Running   1 (2d ago)    2d1h
metrics-server-7db4fb59f9-4nrss      1/1     Running   1 (47h ago)   2d1h

```

### 인증서 설정 확인

```bash
[root@k8s-master ~]# cat ~/.kube/config
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMvakNDQWVhZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJME1Ea3dOREV6TkRjd05Gb1hEVE0wTURrd01qRXpORGN3TkZvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBTjJ4CitwdkJyR3Y3dDBYSVQ4YXZoRzVEV20xR1JaSTN0aERLTnAxQm9EaWVndzd4YitlTGNCK2ZIVTFuWEpXZkxPRHAKWDMzb0F6aVMyN1lKWUNCaEtyUmE4Y2RFclJzcS93cVh5QTg0VERoOHlVSjYrcHF4dFd1R0pKT0pJdmZZRW5PMApJV2h1M2twd3IzNFFHV2tlSGpNRjRnbFgrQVpDQWZYMzZnVEk0OXlnelNFbFV5UDVKMUhsZlhWSDY0UGJzRTdMCkxHZzliTFBXd3FmQnZoNE1VYUIwWUswYTdFVkJmbVZOOGhMV1VaRUJPYW91ZndQYkxET2h0anhiekwyaUxLQjAKaXdnbzk2R3pQYTBjd3o3MWZhMEZyUWJqTTBmbEFNb0JwaFJreUxtVjVYR2thNUN5c1ZhL1lpZ2lVRVFyaTI4VQpPQ0swVmEwd2dFbm5mdjh3RDNzQ0F3RUFBYU5aTUZjd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0hRWURWUjBPQkJZRUZJbEJRRkJmL1VkYzNhSVFPaUVFQXRNWGxPZ3ZNQlVHQTFVZEVRUU8KTUF5Q0NtdDFZbVZ5Ym1WMFpYTXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBSFpSbU5nWCtKYkdCb09ldlhISgpMQ2NWbFJFUUtaSk9EZERON29XT3JsZjhBbUZYVmJPTmJvUy9jdjZJRmZaKzlzSXI4Ujc2MzVhTzdWMFQzRzJKCmM1bm1nNG9VTVVEckdTaGJ6SGl6MHdUSVFieEZTWGRlVVZhSUkzK0hVbkZzeDhKOGpyUjVHRzNvVFFzS001ZHYKd3RZWmtZRGhJWTVMU1lSSmpvaVlhYkRyalkxcjkyenczZUphc3ZGWnNXb3liaUh2dTBxbTBoeVNEY1VzYWVhRwpaRnplNTltS2d1R2RNdzBvOUM2Q29GWE9EQkFodkxMaU8xZlVOTlhoMzg0TmJhWjh2WW9aa01KQ0pmVzY4RUhGCkkzclpPeVBGS0Q0MUtvNjFtbXpKVlRQTnBBbHdkckRJTlR0YVJRSHIzSTNHUyt6cC9lTHBVSVlyekRtcEFsSzAKeWI4PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
    server: https://192.168.56.30:6443 <----- 이게 쿠버 할당 ip + port 와 동일함
```

### Calico 확인

```bash
[root@k8s-master ~]# kubectl get -n calico-system pod
NAME                                       READY   STATUS    RESTARTS   AGE
calico-kube-controllers-658fd9c668-x9qqm   1/1     Running   0          2d1h
calico-node-6jmhk                          1/1     Running   0          2d1h
calico-typha-6b89c57df4-pzt8n              1/1     Running   0          2d1h
csi-node-driver-zfhcd                      2/2     Running   0          2d1h

[root@k8s-master ~]# kubectl get -n calico-apiserver pod
NAME                               READY   STATUS    RESTARTS   AGE
calico-apiserver-97757855f-p6csv   1/1     Running   0          2d1h
calico-apiserver-97757855f-pk4zn   1/1     Running   0          2d1h

[root@k8s-master ~]# kubectl get installations.operator.tigera.io default -o yaml  | grep cidr
      cidr: 20.96.0.0/16
        cidr: 20.96.0.0/16

```