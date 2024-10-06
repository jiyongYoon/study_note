# 5. 배포 파이프라인에서 마주하게 되는 고민들

---

# 1. 인증 정보 관리

ci-cd 서버에는 인증 정보들을 직접 넣어서 사용했기 때문에, 정보들이 남아있다.

## 1) Jenkins에 Credential 설정

<img src="https://github.com/user-attachments/assets/3dc4632f-a756-445a-a002-996f87abc1f6" alt="adder" width="100%" />

- `Dashboard` - `Jenkins 관리` - `Credentials` - `System` - `Global credentials` 에서 Add Credentials로 등록
- kubernetes 인증서는 `Secret file`로 등록

## 2) Jenkins Pipeline에서 Credentials 사용

```groovy
// Docker 사용
steps {
  script{
    withCredentials([usernamePassword(credentialsId: 'docker_password', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
    sh "echo " + '${PASSWORD}' + " | docker login -u " + '${USERNAME}' + " --password-stdin"

// Kubernetes config 사용
steps {
  withCredentials([file(credentialsId: 'k8s_master_config', variable: 'KUBECONFIG')]) {    // 암호화로 관리된 config가 들어감
    sh "kubectl apply -f ./2224/deploy/kubectl/namespace-dev.yaml --kubeconfig " + '${KUBECONFIG}'
    sh "helm upgrade api-tester-2224 ./2224/deploy/helm/api-tester -f ./2224/deploy/helm/api-tester/values-dev.yaml" +
        " -n anotherclass-222-dev --install --kubeconfig " + '${KUBECONFIG}'
```

- `withCredentials([{credentials type(credentialsId: '{등록한id}', variable(Secret file의 경우): '{변수명}', passwordVariable(usernamePassword의 경우): '{변수명}', usernameVariable(usernamePassword의 경우): '{변수명}')])`
- 이후 변수명으로 사용 가능

## 3) 서버에 인증정보 삭제

### docker hub 인증 정보 삭제

```bash
[root@cicd-server .docker]# su - jenkins -s /bin/bash
Last login: Sat Sep 21 05:26:35 KST 2024 on pts/1

[jenkins@cicd-server ~]$ cd ~
[jenkins@cicd-server ~]$ cat ~/.docker/config.json
{
        "auths": {
                "https://index.docker.io/v1/": {
                        "auth": "angkj3qh5oij3q5gixMiE="
                }
        }
}
[jenkins@cicd-server ~]$ docker logout
Removing login credentials for https://index.docker.io/v1/
[jenkins@cicd-server ~]$ cat ~/.docker/config.json
{
        "auths": {}
}
```

### kubernetes 인증서 삭제

- 실습에서는 파일명만 변경하여 사용되지 않도록 함

```bash
[root@cicd-server .kube]# ls -al
total 12
drwxrwxr-x.  3 jenkins jenkins   33 Sep 21 00:52 .
drwxr-xr-x. 20 jenkins jenkins 4096 Sep 21 09:09 ..
drwxr-x---.  4 jenkins jenkins   35 Sep 21 00:52 cache
-rw-------.  1 jenkins jenkins 5641 Sep 21 00:51 config

[root@cicd-server .kube]# mv ./config ./config_bak

[root@cicd-server .kube]# ls -al
total 12
drwxrwxr-x.  3 jenkins jenkins   37 Sep 21 09:13 .
drwxr-xr-x. 20 jenkins jenkins 4096 Sep 21 09:09 ..
drwxr-x---.  4 jenkins jenkins   35 Sep 21 00:52 cache
-rw-------.  1 jenkins jenkins 5641 Sep 21 00:51 config_bak

[root@cicd-server .kube]# kubectl get pods -A
E0921 09:13:56.164741   35598 memcache.go:265] couldn't get current server API group list: <html><head><meta http-equiv='refresh' content='1;url=/login?from=%2Fapi%3Ftimeout%3D32s'/><script id='redirect' data-redirect-url='/login?from=%2Fapi%3Ftimeout%3D32s' src='/static/1eefd20c/scripts/redirect.js'></script></head><body style='background-color:white; color:white;'>
Authentication required
<!--
-->

</body></html>
E0921 09:13:56.166485   35598 memcache.go:265] couldn't get current server API group list: <html><head><meta http-equiv='refresh' content='1;url=/login?from=%2Fapi%3Ftimeout%3D32s'/><script id='redirect' data-redirect-url='/login?from=%2Fapi%3Ftimeout%3D32s' src='/static/1eefd20c/scripts/redirect.js'></script></head><body style='background-color:white; color:white;'>
Authentication required
<!--
-->
...
...
```

# 2. 배포 시 Versioning

## Jenkinsfile

```groovy
environment {
  APP_VERSION = '1.0.1'
  BUILD_DATE = sh(script: "echo `date +%y%m%d.%d%H%M`", returnStdout: true).trim()
  TAG = "${APP_VERSION}-" + "${BUILD_DATA}"

stage('컨테이너 빌드 및 업로드') {
  steps {
	script{
	  // 도커 빌드
      sh "docker build ./2224/build/docker -t 1pro/api-tester:${TAG}"
      sh "docker push 1pro/api-tester:${TAG}"

stage('헬름 배포') {
  steps {
    withCredentials([file(credentialsId: 'k8s_master_config', variable: 'KUBECONFIG')]) {
      sh "helm upgrade api-tester-2224 ./2224/deploy/helm/api-tester -f ./2224/deploy/helm/api-tester/values-dev.yaml" +
         ...
         " --set image.tag=${TAG}"   // Deployment의 Image에 태그 값 주입
```

- `TAG` 변수에 `"${APP_VERSION}-" + "${BUILD_DATA}”` 를 사용하여 값 할당
- 이후에는 `TAG` 변수를 사용
    - docker 이미지에 사용
    - helm 패키지 배포 시 사용

# 3. CI/CD 서버에 빌드된 이미지 관리

- 컨테이너 빌드 및 dockerhub에 업로드 한 후에는 이미지를 삭제해주어 저장공간을 유지한다

    ```groovy
    stage('컨테이너 빌드 및 업로드') {
      steps {
    	script{
    	  // 도커 빌드
          sh "docker build ./${CLASS_NUM}/build/docker -t ${DOCKERHUB_USERNAME}/api-tester:${TAG}"
          sh "docker push ${DOCKERHUB_USERNAME}/api-tester:${TAG}"
          sh "docker rmi ${DOCKERHUB_USERNAME}/api-tester:${TAG}"   // 이미지 삭제
    ```


# 4. 네임스페이스 별도 관리

```groovy
stage('네임스페이스 생성') {  // 배포시 apply로 Namespace 생성 or 배포와 별개로 미리 생성 (추후 삭제시 별도 삭제)
  steps {
    withCredentials([file(credentialsId: 'k8s_master_config', variable: 'KUBECONFIG')]) {
      sh "kubectl apply -f ./2224/deploy/kubectl/namespace-dev.yaml --kubeconfig " + '"${KUBECONFIG}"'
...
stage('헬름 배포') {
  steps {
```

- 네임스페이스는 실제 프로젝트에서는 배포 파이프라인에 들어가있기보다는 최초에 한번 생성하고 쭉 유지하는 경우가 대부분이기 때문에 따로 관리하는게 맞음
    - Master Node에서 직접 kubectl 명령어를 사용해서 만들어도 되고,
    - Jenkins에 Namespace만 만드는 Pipeline을 구축해두어도 좋다.

# 5. Helm 부가 기능

## 1) tag에 latest를 사용하여 배포는 하였지만 Pod가 업그레이드가 안되는 경우

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    metadata:      
      annotations:  
        rollme: {{ randAlphaNum 5 | quote }} // 항상 새 배포를 위해 랜덤값 적용
```

## 2) Pod가 완전 기동 될때까지 결과값을 기다림

- jenkins 배포 서버의 스레드가 계속 해당 작업에 할당되어있을수는 없기 때문에 timeout이 필요함

    ```groovy
    stage('헬름 배포') {
      steps {
        withCredentials([file(credentialsId: 'k8s_master_config', variable: 'KUBECONFIG')]) {
          sh "helm upgrade api-tester-2224 ./2224/deploy/helm/api-tester -f ./2224/deploy/helm/api-tester/values-dev.yaml" +
             ...
             " --wait --timeout=10m" +  // 최대 10분으로 설정
    ```


# 6. kubernetes에서 사용하지 않는 이미지 관리

- 기본적으로 kubernetes garbage collecter가 작동함

    ```bash
    # GC 속성 추가하기
    [root@k8s-master ~]# vi /var/lib/kubelet/config.yaml
    -----------------------------------
    imageMinimumGCAge : 3m // 이미지 생성 후 해당 시간이 지나야 GC 대상이 됨 (Default : 2m)
    imageGCHighThresholdPercent : 80 // Disk가 80% 위로 올라가면 GC 수행 (Default : 85)
    imageGCLowThresholdPercent : 70 // Disk가 70% 밑으로 떨어지면 GC 수행 안함(Default : 80)
    -----------------------------------
    
    # kubelet 재시작하여 속성 적용
    [root@k8s-master ~]# systemctl restart kubelet
    ```

    - 아무 속성도 세팅하지 않는 경우 기본값
        - imageMinimumGCAge: 2m
        - imageGCHighThresholdPercent: 85
        - imageGCLowThresholdPercent: 80