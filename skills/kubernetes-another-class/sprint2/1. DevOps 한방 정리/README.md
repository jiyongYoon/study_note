# 1. DevOps 한방 정리

---

<img src="https://github.com/user-attachments/assets/4ae95c68-2112-40ab-b6ec-80834a7fcce1" alt="adder" width="100%" />

- DevOps는 개발 (Dev) 부터 운영 (Operation) 까지의 프로세스 전반에 대한 개발 방법론으로, 소프트웨어의 배포 주기가 짧아져야 함에 따라 효율적으로 애플리케이션을 빌드 및 배포하기 위해 만들어졌다고 한다.
- 결국 개발된 소프트웨어가 고객에게 전달하기 위한 과정인데, 이 과정에서 핵심이 되는 내용을 꼽으라면 바로 `개발` - `빌드` - `실행파일배포` 이 세 단계가 될 것이다.
- 쿠버네티스는 DevOps 전체 프로세스에서 CD와 운영까지의 부분에 관여하게 된다.

### kubernetes와 CD

쿠버네티스 환경에서는 애플리케이션이 컨테이너 형태인 Pod로 배포되기 때문에, 소스 빌드를 한 후 컨테이너 이미지를 만들어야 한다.

이 컨테이너 이미지를 만드는 부분까지를 CI이라고 생각한다.

그러면 kubernetes에서는 helm이나 kustomize 를 통해 배포 명령을 받게 되고, container registry(e.g. Dockerhub 등) 에서 컨테이너를 받아온 후 세팅에 맞는 배포를 하는 과정을 진행한다. 이 부분이 kubernetes에서의 CD 과정으로 생각했다.

### kubernetes와 Ops

쿠버네티스 클러스터는 여러 Node에 여러 Pod 들이 떠있고, 이를 중앙(Master Node)에서 일괄 관리한다. 이는 각 Node에 있는 컨테이너들을 직접 관리하지 않아 운영을 편리하게 해주며, kubernetes 세팅에 따라 auto healing, auto scaling, 그리고 배포 방법까지도 지원을 한다.