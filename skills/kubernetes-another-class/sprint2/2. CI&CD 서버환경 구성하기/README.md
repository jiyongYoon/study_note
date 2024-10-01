# 2. CI/CD 서버환경 구성하기

---

## 1) Vagrant 스크립트 실행하여 vm 설치

```bash
# Vagrant 폴더 생성
C:\Users\사용자> mkdir cicd
C:\Users\사용자> cd cicd

# Vagrant 스크립트 다운로드
C:\Users\사용자\cicd> curl -O https://raw.githubusercontent.com/k8s-1pro/install/main/ground/cicd-server/vagrant-2.3.4/Vagrantfile

# Rocky Linux Repo 세팅
C:\Users\사용자\cicd> curl -O https://raw.githubusercontent.com/k8s-1pro/install/main/ground/cicd-server/vagrant-2.3.4/rockylinux-repo.json
C:\Users\사용자\cicd> vagrant box add rockylinux-repo.json

# Vagrant Vbguest 및 Disk Plugin 설치 
C:\Users\사용자\cicd> vagrant plugin install vagrant-vbguest vagrant-disksize

# Vagrant 실행
C:\Users\사용자\cicd> vagrant up
```

### 사용되는 Vagrant 스크립트 내용

```bash
Vagrant.configure("2") do |config|

  # OS 선택
  config.vm.box = "rockylinux/8"
  # Disk 확장
  config.disksize.size = "30GB"
  # 자동 업데이트 안함
  config.vbguest.auto_update = false
  # PC-VM간 마운팅 안함
  config.vm.synced_folder "./", "/vagrant", disabled: true

  config.vm.define "cicd-server" do |cicd|
    cicd.vm.hostname = "cicd-server"
    cicd.vm.network "private_network", ip: "192.168.56.20"
	cicd.vm.provider :virtualbox do |vb|
      vb.memory = 2048
      vb.cpus = 2
	  vb.customize ["modifyvm", :id, "--firmware", "efi"]
	end
    cicd.vm.provision :shell, privileged: true, inline: $install_cicd
  end
end

$install_cicd = <<-SHELL

echo '======== [1] Rocky Linux 기본 설정 ========'
echo '======== [1-1] 패키지 업데이트 ========'
# 강의와 동일한 실습 환경을 유지하기 위해 Linux Update 주석 처리
# yum -y update

echo '======== [1-2] 타임존 설정 ========'
timedatectl set-timezone Asia/Seoul

echo '======== [1-3] Disk 확장 설정 ========'
yum install -y cloud-utils-growpart
growpart /dev/sda 4
xfs_growfs /dev/sda4

echo '======== [1-4] 방화벽 해제 ========'
systemctl stop firewalld && systemctl disable firewalld

echo '======== [2] Kubectl 설치 ========'
echo '======== [2-1] repo 설정 ========'
cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://pkgs.k8s.io/core:/stable:/v1.27/rpm/
enabled=1
gpgcheck=1
gpgkey=https://pkgs.k8s.io/core:/stable:/v1.27/rpm/repodata/repomd.xml.key
exclude=kubelet kubeadm kubectl cri-tools kubernetes-cni
EOF

echo '======== [2-2] Kubectl 설치 ========'
yum install -y kubectl-1.27.2-150500.1.1.x86_64 --disableexcludes=kubernetes

echo '======== [3] 도커 설치 ========'
# https://download.docker.com/linux/centos/8/x86_64/stable/Packages/ 저장소 경로
yum install -y yum-utils
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum install -y docker-ce-3:23.0.6-1.el8 docker-ce-cli-1:23.0.6-1.el8 containerd.io-1.6.21-3.1.el8
systemctl daemon-reload
systemctl enable --now docker

echo '======== [4] OpenJDK 설치  ========'
# yum list --showduplicates java-17-openjdk
yum install -y java-17-openjdk

echo '======== [5] Gradle 설치  ========'
yum -y install wget unzip
wget https://services.gradle.org/distributions/gradle-7.6.1-bin.zip -P ~/
unzip -d /opt/gradle ~/gradle-*.zip
cat <<EOF |tee /etc/profile.d/gradle.sh
export GRADLE_HOME=/opt/gradle/gradle-7.6.1
export PATH=/opt/gradle/gradle-7.6.1/bin:${PATH}
EOF
chmod +x /etc/profile.d/gradle.sh
source /etc/profile.d/gradle.sh

echo '======== [6] Git 설치  ========'
yum install -y git-2.43.0-1.el8

echo '======== [7] Jenkins 설치  ========'
wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
yum install -y jenkins-2.440.2-1.1
systemctl enable jenkins
systemctl start jenkins

SHELL
```

- Jenkins 2.440 버전은 java 17 버전에서 돌아가기 때문에 jdk를 추가로 더 설치하지 않아도 된다.

## 2) ci-cd vm 접속

- 기본 id / pw : root / vagrant

## 3) Jenkins 초기 세팅

### (1) Jenkins 초기 비밀번호 확인

```bash
[root@cicd-server ~]# cat /var/lib/jenkins/secrets/initialAdminPassword
b10efae435374ccbafd65e34c3ca2bf7  <-- jenkins unlock password
```

### (2) 대시보드에 접속하여 비밀번호 확인 및 플러그인 설치

- url: http://192.168.56.20:8080/login

### (3) Admin 사용자 생성 및 Jenkins 시작

- [Save and Finish] → [Start using Jenkins]

## 4) Jenkins 전역 설정 (JDK, Gradle)

- Jenkins에 우리가 vm에 설치한 JDK와 Gradle을 사용하도록 설정하는 것
- 위치: [Dashboard] → [Jenkins 관리] → [Tools]

### (1) JDK 세팅

- name: jdk-17
- JAVA_HOME
    - 확인 방법

        ```bash
        # java 실행파일 찾기
        [root@cicd-server ~]# which java
        /bin/java
        
        # java의 원래 위치 찾아가기
        [root@cicd-server bin]# ll | grep java
        lrwxrwxrwx. 1 root root       26 Sep 21 00:11 alt-java -> /etc/alternatives/alt-java
        lrwxrwxrwx. 1 root root       22 Sep 21 00:11 java -> /etc/alternatives/java
        
        [root@cicd-server bin]# cd /etc/alternatives
        [root@cicd-server alternatives]# ll | grep java
        lrwxrwxrwx. 1 root root 62 Sep 21 00:11 java -> /usr/lib/jvm/java-17-openjdk-17.0.12.0.7-2.el8.x86_64/bin/java
        
        [root@cicd-server alternatives]# cd /usr/lib/jvm/java-17-openjdk-17.0.12.0.7-2.el8.x86_64/bin
        [root@cicd-server bin]# ll
        total 80
        -rwxr-xr-x. 1 root root 16304 Jul 18 07:31 alt-java
        -rwxr-xr-x. 1 root root 15824 Jul 18 07:31 java   <--- 더이상 심볼릭 링크가 나오지 않은 요놈이 진짜 실행파일임
        -rwxr-xr-x. 1 root root 15848 Jul 18 07:31 jcmd
        -rwxr-xr-x. 1 root root 15856 Jul 18 07:31 keytool
        -rwxr-xr-x. 1 root root 15856 Jul 18 07:31 rmiregistry
        ```


<img src="https://github.com/user-attachments/assets/84116403-9a3c-47cc-9dea-e758360ea902" alt="adder" width="70%" />

### (2) Gradle 세팅

- name: gradle-7.6.1
- GRADLE_HOME: 확인

<img src="https://github.com/user-attachments/assets/686ae914-bfdc-4ccc-ab77-60ff814b5837" alt="adder" width="70%" />

## 5) DockerHub 세팅

- 회원가입

## 6) Docker 사용 설정

```bash
# jeknins가 Docker를 사용할 수 있도록 권한 부여
[root@cicd-server ~]# chmod 666 /var/run/docker.sock
[root@cicd-server ~]# usermod -aG docker jenkins

# Jeknins로 사용자 변경 
[root@cicd-server ~]# su - jenkins -s /bin/bash

# 자신의 Dockerhub로 로그인 하기
[jenkins@cicd-server ~]$ docker login
Login with your Docker ID to push and pull images from Docker Hub. If you don't have a Docker ID, head over to https://hub.docker.com to create one.
Username: jyyoon0615
Password:
WARNING! Your password will be stored unencrypted in /var/lib/jenkins/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
```

- /var/run/docker.sock 파일은 도커 데몬(컨테이너를 관리하는 백그라운드 프로세스)과 도커 클라이언트(도커 데몬과 상호작용하는 사용자 인터페이스) 사이의 통신을 위한 소켓 파일.
- `Your password will be stored unencrypted in /var/lib/jenkins/.docker/config.json.` 여기 위치에 비밀번호가 저장되기 때문에 vm을 껐다 켜도 로그인이 되어있다.
- **Jenkins 파이프라인을 구축하면 Credential을 사용하여 disk 저장 없이도 사용이 가능해진다.**

## 7) Master Node에서 인증서 복사

- Jenkins CI/CD 배포 시 Kubectl을 사용하여 쿠버네티스에 api를 날리기 위해 필요

```bash
# 폴더 생성
[jenkins@cicd-server ~]$ mkdir ~/.kube

# Master Node에서 인증서 가져오기
[jenkins@cicd-server ~]$ scp root@192.168.56.30:/root/.kube/config ~/.kube/
```

<img src="https://github.com/user-attachments/assets/8f53ad0f-37dd-425b-8ea5-abb5515b0137" alt="adder" width="100%" />

인증서 복사가 완료된 후 kubectl 명령어가 잘 동작함

## 8) Github 가입, Repo fork 및 Deployment.yaml 수정

- CI/CD 배포 시 내 dockerhub에서 이미지를 가져오도록 repo명 변경

## 9) Build를 위한 Jenkins 스크립트 작성

### Freestyle project로 생성

- Github project
    - 소스코드가 있는 github repo 세팅
- 소스 코드 관리
    - 소스코드가 있는 github repo 세팅
    - branch 세팅 - `*/main`
- Build Steps
    - Add build step → `Invoke Gradle script` → Gradle version 세팅 (전역으로 설정한 gradle name이 보임)
- 저장하기
- 지금 빌드

<img src="https://github.com/user-attachments/assets/242fb994-c477-4e5f-92a2-eff80b71b37f" alt="adder" width="100%" />

빌드가 되어 jar 파일이 생성된 모습

## 10) 컨테이너 빌드

### Freestyle project로 생성

- Github project
    - 빌드관련 Dockerfile, yaml 파일이 있는 github repo 세팅
- 소스 코드 관리
    - 빌드관련 Dockerfile, yaml 파일이 있는 github repo 세팅
    - branch 세팅 - `*/main`
    - `Additional Behaviours` → `Sparse Checkout paths` : 이 위치의 소스코드만 다운로드 하겠다는 뜻

        <img src="https://github.com/user-attachments/assets/34af1141-b0fc-4af4-8caf-69f01af860a9" alt="adder" width="60%" />

- Build Steps
    - `Add build step` → `Execute shell`

        ```bash
        # jar 파일 복사
        cp /var/lib/jenkins/workspace/2121-source-build/build/libs/app-0.0.1-SNAPSHOT.jar ./2121/build/docker/app-0.0.1-SNAPSHOT.jar
        
        # 도커 빌드
        docker build -t jyyoon0615/api-tester:v1.0.0 ./2121/build/docker
        docker push jyyoon0615/api-tester:v1.0.0
        ```

        - 해당 Shell 스크립트를 실행하여 빌드를 진행하도록 함
        - 이 스크립트가 실행되는 위치는 jenkins 유저의 home의 workspace의 project 명 폴더임

          <img src="https://github.com/user-attachments/assets/2ccf98db-1ccc-41c8-bf2c-e9db09abd93e" alt="adder" width="60%" />

            - 즉 `2121-container-build` 디렉토리의 Root가 바로 Github project 소스코드의 Root가 되는 것이며, 여기에 빌드된 jar 파일을 이동시킴

              <img src="https://github.com/user-attachments/assets/d09c8a0b-b0ab-4b36-b8b1-ac7dab34af47" alt="adder" width="60%" />

        - Dockerfile은 아래와 같이 생김

            ```docker
            FROM openjdk:17
            COPY ./app-0.0.1-SNAPSHOT.jar /usr/src/myapp/app.jar
            ENTRYPOINT ["java", "-Dspring.profiles.active=${spring_profiles_active}", "-Dapplication.role=${application_role}", "-Dpostgresql.filepath=${postgresql_filepath}", "-jar", "/usr/src/myapp/app.jar"]
            EXPOSE 8080
            WORKDIR /usr/src/myapp
            ```

<img src="https://github.com/user-attachments/assets/a84ea7e5-8462-42ac-a91f-415aeb93bb86" alt="adder" width="80%" />

DockerHub에 Push 완료됨

## 11) 쿠버네티스 배포

### 기존 프로젝트 복사하여 생성

- Docker 이미지 빌드하는 CI/CD와 사용하는 github Repository가 동일하여 복사해서 생성
- 변경되는 내용
    - 소스 코드 관리에서 `Sparse Checkout paths`를 `/2121/deploy/k8s` 쿠버네티스 배포 파일이 있는 위치로 변경
    - Build Steps에서 `Execute shell` 내용을 변경

        ```bash
        kubectl apply -f ./2121/deploy/k8s/namespace.yaml
        kubectl apply -f ./2121/deploy/k8s/pv.yaml
        kubectl apply -f ./2121/deploy/k8s/pvc.yaml
        kubectl apply -f ./2121/deploy/k8s/configmap.yaml
        kubectl apply -f ./2121/deploy/k8s/secret.yaml
        kubectl apply -f ./2121/deploy/k8s/service.yaml
        kubectl apply -f ./2121/deploy/k8s/hpa.yaml
        kubectl apply -f ./2121/deploy/k8s/deployment.yaml
        ```


<img src="https://github.com/user-attachments/assets/5a822eea-90bf-4899-a5a6-7beb36017cf9" alt="adder" width="80%" />

kubernetes 배포 완료!