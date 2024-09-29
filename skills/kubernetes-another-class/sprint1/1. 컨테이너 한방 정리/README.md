# 1. 컨테이너 한방 정리

---

# Container Orchestration, Container, Linux OS와의 관계

<img src="https://github.com/user-attachments/assets/b4cb7dcf-f39f-4e4c-b2ac-4d82017586dc" alt="adder" width="100%" />

## Linux OS

크게 두 계열이 존재

| 항목 | 계열 | 대표 OS | 비고 |
| --- | --- | --- | --- |
| 무료 | debian | ubuntu (debian 확장판) | ubuntu 점유율 가장 높음 |
| 유료 | Red Hat | CentOS (7, 8 모두 무료 종료) | 무료 사용 가능한 복제판 존재 (Rocky Linux - 복제판 중 가장 활성) |

## Container

커널 level의 chroot(유저, 파일, 네트워크 격리), cgroup(cpu, memory 격리), namespace(프로세스 격리)로 격리 기능이 확장되며 커널 level의 기술들을 활용하는 Low level의  `LXC(Linux Container)` 가 기능적으로 완성됨.

LXC 기반으로 Docker 엔진이 개발되었으나, 현재는 LXC가 아닌 `runC`를 사용하여 커널 level 기술을 직접 사용함. (아래 Container Runtime에서 자세하게 다룰 것)

containerd는 도커 엔진에서 컨테이너 생성 기능만 따로 분리한 것을 말함.

## Container Orchestration

컨테이너를 쉽게 사용하도록 돕는 도구. 따라서 컨테이너와 `인터페이스 호환성`이 매우 중요하다.

kubernetes, docker swarm, Nomad, .. 있었지만 kubernetes로 천하통일. 기업 관리형으로 확장된 Orchestration 환경은 있긴 함.

# Container Runtime

<img src="https://github.com/user-attachments/assets/9f9859e9-3a75-4207-94a7-849d4f14adbf" alt="adder" width="100%" />

### Orchestration 파트

`kube-apiserver` - ‘쿠버네티스 컨트롤 플레인의 프론트 엔드’라고 생각하면 됨.

`kubelet` - kube-apiserver 로 전송된 명령을 container runtime에 보내는 역할. Orchestration 측에서 유저의 명령을 처리하는 주체라고 생각하면 됨.

- 현재: kublet에는 각 컨테이너 런타임의 기능을 사용하기 위한 Interface인 `CRI(Container Runtime Interface)`가 존재하고, 이 구현을 각각의 컨테이너 런타임측에서 플러그인 형태로 구현하는 방식으로 컨테이너를 관리함. `v1.27` 부터 stable 하게 관리되고 있음.

### Container 파트

`container runtime` - 컨테이너를 생성하는 동작을 하는 부분. High level에는 docker 엔진, containerd, cri-o 등이 있으며, Low level은 LXC, runC, rkt 등이 있음.

- OCI(Open Container Initiative): 2017년 시작됨. 컨테이너 표준을 관리하고, 이 규약을 지키면 어떤 컨테이너 런타임에서도 컨테이너가 동작될 수 있도록 관리됨.
- 현재: High level의 containerd, cri-o는 Low level 의 runC를 사용하여 Kernel level의 기능을 사용함. High level의 docker engine은 미란티스에 인수되어 미란티스 컨테이너 런타임에서 Kernel level의 기능을 사용함.