# 6. ArgoCD

---

# 1. Agro CD 아키텍처

<img src="https://github.com/user-attachments/assets/112180dc-66e8-49ae-bc76-896c7f8afc0e" alt="adder" width="100%" />
- Argo 의 여러 툴들이 있다.

## 1) Argo CD

- kubernetes 전용 배포 툴이며 Git repository가 반드시 필요하다.
- Master Node에 설치되어 Git repository에 배포된 yaml 파일을 사용하여 kubernetes에 리소스들을 편하게 배포하게 도와준다.

## 2) Argo Image Updater

- 컨테이너 이미지의 변경을 감지하여 배포를 진행한다.

## 3) Rollouts

- blue - green, canary 등의 배포 방법을 지원한다.

# 2. Argo Apps 설치 및 배포 (kubectl, helm)

<img src="https://github.com/user-attachments/assets/3b937307-2cb5-4418-9683-bf1cd6ab192e" alt="adder" width="100%" />

## 1) Artifact HUB에서 Argo Helm Chart 다운로드 및 github 업로드

```bash
# helm이 설치돼 있는 서버에서 작업
# helm 레포지토리(argo-cd) 설정 및 다운로드
helm repo add argo https://argoproj.github.io/argo-helm
helm pull argo/argo-cd --version 5.52.1
helm pull argo/argocd-image-updater --version 0.9.2
helm pull argo/argo-rollouts --version 2.34.1

# 압축 해제
tar -xf argo-cd-5.52.1.tgz
tar -xf argocd-image-updater-0.9.2.tgz
tar -xf argo-rollouts-2.34.1.tgz

# 내용 확인
ls argo*
------
argo-cd-5.52.1.tgz  argocd-image-updater-0.9.2.tgz  argo-rollouts-2.34.1.tgz
argo-cd:
Chart.lock  charts  Chart.yaml  README.md  templates  values.yaml
argocd-image-updater:
Chart.yaml  README.md  templates  values.yaml
argo-rollouts:
Chart.yaml  README.md  templates  values.yaml

# helm package를 Github로 업로드
https://github.com/k8s-1pro/install/tree/main/ground/cicd-server/argo/helm/argo-cd
https://github.com/k8s-1pro/install/tree/main/ground/cicd-server/argo/helm/argocd-image-updater
https://github.com/k8s-1pro/install/tree/main/ground/cicd-server/argo/helm/argocd-rollouts
```

<img src="https://github.com/user-attachments/assets/31854147-184d-4025-bf37-7f739167e445" alt="adder" width="50%" />

## 2) Helm으로 Argo CD를 설치하는 Jenkins Pipeline

### Jenkinsfile

```groovy
pipeline {
    agent any

    parameters {
        choice(choices: ['option', 'namespace_create', 'namespace_delete', 'helm_upgrade', 'helm_uninstall'], name: 'DEPLOY_TYPE', description: '배포 타입 선택')
        choice(choices: ['option', 'argo-cd', 'argocd-image-updater', 'argo-rollouts'], name: 'TARGET_ARGO', description: 'Argo 대상 선택')
    }

    environment {
        DOCKERHUB_USERNAME = '1pro'
        GITHUB_URL = 'https://github.com/k8s-1pro/install.git'
        INSTALL_PATH = 'ground/cicd-server/argo'
    }

    stages {
        stage('네임스페이스 관리') {
            steps {
                script{
                    if (params.DEPLOY_TYPE == "namespace_create") {
                        withCredentials([file(credentialsId: 'k8s_master_config', variable: 'KUBECONFIG')]) {
                            sh "kubectl apply -f ./${INSTALL_PATH}/kubectl/namespace.yaml --kubeconfig " + '${KUBECONFIG}'
                        }
                    } else if (params.DEPLOY_TYPE == "namespace_delete") {
                        withCredentials([file(credentialsId: 'k8s_master_config', variable: 'KUBECONFIG')]) {
                            sh "kubectl delete -f ./${INSTALL_PATH}/kubectl/namespace.yaml --kubeconfig " + '${KUBECONFIG}'
                        }
                    } else {
                        echo "skip namespace"
                    }
                }
            }
        }

        stage('헬름 배포 관리') {
            steps {
                script{
                    if (params.DEPLOY_TYPE == "helm_upgrade") {
                        withCredentials([file(credentialsId: 'k8s_master_config', variable: 'KUBECONFIG')]) {
                            HELM_DEPLOY_COMMAND =  "helm upgrade ${params.TARGET_ARGO} ./${INSTALL_PATH}/helm/${params.TARGET_ARGO} " +
                                " -f ./${INSTALL_PATH}/helm/${params.TARGET_ARGO}/values-dev.yaml" +
                                " -n argo --install --kubeconfig " + '${KUBECONFIG}' +
                                " --wait --timeout=10m "   // 최대 10분으로 설정

                            // image-updater일 경우 도커허브 credentials 주입
                            if (params.TARGET_ARGO == "argocd-image-updater") {
                                // https://argocd-image-updater.readthedocs.io/en/stable/basics/update-methods/
                                withCredentials([usernamePassword(credentialsId: 'docker_password', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                                    HELM_DEPLOY_COMMAND += " --set config.registries[0].credentials=env:DOCKER_HUB_CREDS="+ '${USERNAME}' + ":" + '${PASSWORD}'
                                }
                            }

                            sh "eval ${HELM_DEPLOY_COMMAND}"
                            // sh "helm update argo-cd -n argo --kubeconfig " + '${KUBECONFIG}' + " --wait --timeout=10m "
                        }

                    } else if (params.DEPLOY_TYPE == "helm_uninstall") {
                        withCredentials([file(credentialsId: 'k8s_master_config', variable: 'KUBECONFIG')]) {
                            sh "helm uninstall ${params.TARGET_ARGO} -n argo --kubeconfig " + '${KUBECONFIG}'

                            // CRD 삭제
                            if (params.TARGET_ARGO == "argo-cd") {
                                sh "kubectl delete crd applications.argoproj.io applicationsets.argoproj.io appprojects.argoproj.io --kubeconfig " + '${KUBECONFIG}'
                            }
                            if (params.TARGET_ARGO == "argo-rollouts") {
                                sh "kubectl delete crd analysisruns.argoproj.io analysistemplates.argoproj.io clusteranalysistemplates.argoproj.io --kubeconfig " + '${KUBECONFIG}'
                                sh "kubectl delete crd experiments.argoproj.io rollouts.argoproj.io --kubeconfig " + '${KUBECONFIG}'
                            }
                        }
                    } else {
                        echo "skip deploy"
                    }
                }
            }
        }
    }
}
```

### values-dev.yaml 적용

```yaml
## Server
server:
  service:
    type: NodePort
    nodePortHttps: 30002
```

## 3) Argo CD 접속

`http://192.168.56.30:30002/login` 접속

## 4) Argo CD 초기 비밀번호 확인

```bash
kubectl get -n argo secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d
```

# 3. Argo CD에서 App 배포하기

## 1) kubectl 사용하기

### (1)`Applications` - `+ NEW APP` 으로 App 추가

- **Project Name**
    - kubernetes의 namespace와 같은 argo cd가 관리하는 리소스로, kubernetes와 마찬가지로 default project가 존재한다.
- **Sync Options**
    - **SKIP SCHEMA VALIDATION :** 매니패스트에 대한 yaml 스키마 유효성 검사를 건너뛰고 배포 (kubectl apply --validate=false)
    - **PRUNE LAST :** 동기화 작업이 끝난 이후에 Prune(git에 없는 리소스를 제거하는 작업)를 동작시킴
    - **RESPECT IGNORE DIFFERENCES :** 동기화 상태에서 특정 상태의 필드를 무시하도록 함
    - **AUTO-CREATE NAMESPACE :** 클러스터에 네임스페이스가 없을 시 argocd에 입력한 이름으로 자동 생성
    - **APPLY OUT OF SYNC ONLY :** 현재 동기화 상태가 아닌 리소스만 배포
    - **SERVER-SIDE APPLY  :** 쿠버네티스 서버에서 제공하는 Server-side Apply API 기능 활성화 (레퍼런스 참조)
- **Prune Propagation Policy**
    - **foreground :** 부모(소유자, ex. deployment) 자원을 먼저 삭제함
    - **background  :** 자식(종속자, ex. pod) 자원을 먼저 삭제함
    - **orphan  :** 고아(소유자는 삭제됐지만, 종속자가 삭제되지 않은 경우) 자원을 삭제함
- **Source**
    - 배포할 소스코드
- **Destination**
    - Cluster URL : `https://kubernetes.default.svc` → kube-apiserver로 가는 도메인 이름.

      <img src="https://github.com/user-attachments/assets/6e849b2c-c7e7-478a-97ba-e278ea7f9b2e" alt="adder" width="100%" />

    - Namespace
        - 생성할 Namespace
    - Directory
        - `Source.Path`디렉토리에 있는 모든 yaml 파일들을 배포 대상으로 한다는 뜻

### (2) Sync (배포)

<img src="https://github.com/user-attachments/assets/e01bcd02-71f0-44d8-9f43-60758fb3120e" alt="adder" width="50%" />

- 배포 추가 옵션
    - **PRUNE :** GIt에서 자원 삭제 후 배포시 K8S에서는 삭제되지 않으나, 해당 옵션을 선택하면 삭제시킴
    - **FORCE :** --force 옵션으로 리소스 삭제
    - **APPLY ONLY :** ArgoCD의 Pre/Post Hook은 사용 안함 (리소스만 배포)
    - **DRY RUN :** 테스트 배포 (배포에 에러가 있는지 한번 확인해 볼때 사용)

### (3) 배포 확인

<img src="https://github.com/user-attachments/assets/b518f8e1-8c1c-48b5-9c30-e06ce1c3811d" alt="adder" width="100%" />

## 2) Helm 사용하기

### (1) (나머지는 kubectl과 동일하며) Directory 대신 Helm 선택하기

<img src="https://github.com/user-attachments/assets/c053d097-324b-4671-b054-978cad117c13" alt="adder" width="30%" />

- VALUES FILES: 사용할 values.yaml 파일

### (2) Sync (배포)

### (3) 배포 확인

<img src="https://github.com/user-attachments/assets/7ffc2a19-3ce7-4b7b-ab93-8ac45f9faa17" alt="adder" width="100%" />

# 4. Argo CD Image Updater 사용하기

<img src="https://github.com/user-attachments/assets/c3b85715-cb31-47cb-bb2b-9ef3d2102d28" alt="adder" width="100%" />

## 1) Jenkins Pipeline으로 Image Updater 설치

### 에러 발생

- 1) Jenkins pipeline의 첫 번째 에러

  ```bash
  Obtained ground/cicd-server/argo/Jenkinsfile from git https://github.com/k8s-1pro/install.git
  [Pipeline] Start of Pipeline
  [Pipeline] node
  Running on Jenkins in /var/lib/jenkins/workspace/deploy-argo
  [Pipeline] {
  [Pipeline] stage
  [Pipeline] { (Declarative: Checkout SCM)
  [Pipeline] checkout
  The recommended git tool is: NONE
  No credentials specified
   > git rev-parse --resolve-git-dir /var/lib/jenkins/workspace/deploy-argo/.git # timeout=10
  Fetching changes from the remote Git repository
   > git config remote.origin.url https://github.com/k8s-1pro/install.git # timeout=10
  Fetching upstream changes from https://github.com/k8s-1pro/install.git
   > git --version # timeout=10
   > git --version # 'git version 2.43.0'
   > git fetch --tags --force --progress -- https://github.com/k8s-1pro/install.git +refs/heads/*:refs/remotes/origin/* # timeout=10
   > git rev-parse refs/remotes/origin/main^{commit} # timeout=10
  Checking out Revision 246a8ab99b1d9252ed206818a85dac544bc00301 (refs/remotes/origin/main)
   > git config core.sparsecheckout # timeout=10
   > git read-tree -mu HEAD # timeout=10
   > git checkout -f 246a8ab99b1d9252ed206818a85dac544bc00301 # timeout=10
  Commit message: "git update"
   > git rev-list --no-walk 246a8ab99b1d9252ed206818a85dac544bc00301 # timeout=10
  [Pipeline] }
  [Pipeline] // stage
  [Pipeline] withEnv
  [Pipeline] {
  [Pipeline] withEnv
  [Pipeline] {
  [Pipeline] stage
  [Pipeline] { (네임스페이스 관리)
  [Pipeline] script
  [Pipeline] {
  [Pipeline] echo
  skip namespace
  [Pipeline] }
  [Pipeline] // script
  [Pipeline] }
  [Pipeline] // stage
  [Pipeline] stage
  [Pipeline] { (헬름 배포 관리)
  [Pipeline] script
  [Pipeline] {
  [Pipeline] withCredentials
  Masking supported pattern matches of $KUBECONFIG
  [Pipeline] {
  [Pipeline] withCredentials
  Masking supported pattern matches of $PASSWORD
  [Pipeline] {
  [Pipeline] }
  [Pipeline] // withCredentials
  [Pipeline] sh
  + eval helm upgrade argocd-image-updater ./ground/cicd-server/argo/helm/argocd-image-updater -f ./ground/cicd-server/argo/helm/argocd-image-updater/values-dev.yaml -n argo --install --kubeconfig **** --wait --timeout=10m --set 'config.registries[0].credentials=env:DOCKER_HUB_CREDS=:'
  ++ helm upgrade argocd-image-updater ./ground/cicd-server/argo/helm/argocd-image-updater -f ./ground/cicd-server/argo/helm/argocd-image-updater/values-dev.yaml -n argo --install --kubeconfig **** --wait --timeout=10m --set 'config.registries[0].credentials=env:DOCKER_HUB_CREDS=:'
  Release "argocd-image-updater" does not exist. Installing it now.
  Error: Get "https://192.168.56.30:6443/apis/apps/v1/namespaces/argo/replicasets?labelSelector=app.kubernetes.io%2Finstance%3Dargocd-image-updater%2Capp.kubernetes.io%2Fname%3Dargocd-image-updater": http2: client connection lost
  [Pipeline] }
  [Pipeline] // withCredentials
  [Pipeline] }
  [Pipeline] // script
  [Pipeline] }
  [Pipeline] // stage
  [Pipeline] }
  [Pipeline] // withEnv
  [Pipeline] }
  [Pipeline] // withEnv
  [Pipeline] }
  [Pipeline] // node
  [Pipeline] End of Pipeline
  ERROR: script returned exit code 1
  Finished: FAILURE
  ```

    - 커넥션 에러가 발생했다.  kubernetes 클러스터에 Pod들이 많이 떠있어서 문제가 있었던 것 같다. 대시보드도 잘 안들어가지고 명령어도 겨우겨우 동작했기 때문이다. 일단 안쓰던 리소스들을 제거해주고 다시 시도했다.
- 2) helm update 작업중

  ```bash
  Obtained ground/cicd-server/argo/Jenkinsfile from git https://github.com/k8s-1pro/install.git
  [Pipeline] Start of Pipeline
  [Pipeline] node
  Running on Jenkins in /var/lib/jenkins/workspace/deploy-argo
  [Pipeline] {
  [Pipeline] stage
  [Pipeline] { (Declarative: Checkout SCM)
  [Pipeline] checkout
  The recommended git tool is: NONE
  No credentials specified
   > git rev-parse --resolve-git-dir /var/lib/jenkins/workspace/deploy-argo/.git # timeout=10
  Fetching changes from the remote Git repository
   > git config remote.origin.url https://github.com/k8s-1pro/install.git # timeout=10
  Fetching upstream changes from https://github.com/k8s-1pro/install.git
   > git --version # timeout=10
   > git --version # 'git version 2.43.0'
   > git fetch --tags --force --progress -- https://github.com/k8s-1pro/install.git +refs/heads/*:refs/remotes/origin/* # timeout=10
   > git rev-parse refs/remotes/origin/main^{commit} # timeout=10
  Checking out Revision 246a8ab99b1d9252ed206818a85dac544bc00301 (refs/remotes/origin/main)
   > git config core.sparsecheckout # timeout=10
   > git read-tree -mu HEAD # timeout=10
   > git checkout -f 246a8ab99b1d9252ed206818a85dac544bc00301 # timeout=10
  Commit message: "git update"
   > git rev-list --no-walk 246a8ab99b1d9252ed206818a85dac544bc00301 # timeout=10
  [Pipeline] }
  [Pipeline] // stage
  [Pipeline] withEnv
  [Pipeline] {
  [Pipeline] withEnv
  [Pipeline] {
  [Pipeline] stage
  [Pipeline] { (네임스페이스 관리)
  [Pipeline] script
  [Pipeline] {
  [Pipeline] echo
  skip namespace
  [Pipeline] }
  [Pipeline] // script
  [Pipeline] }
  [Pipeline] // stage
  [Pipeline] stage
  [Pipeline] { (헬름 배포 관리)
  [Pipeline] script
  [Pipeline] {
  [Pipeline] withCredentials
  Masking supported pattern matches of $KUBECONFIG
  [Pipeline] {
  [Pipeline] withCredentials
  Masking supported pattern matches of $PASSWORD
  [Pipeline] {
  [Pipeline] }
  [Pipeline] // withCredentials
  [Pipeline] sh
  + eval helm upgrade argocd-image-updater ./ground/cicd-server/argo/helm/argocd-image-updater -f ./ground/cicd-server/argo/helm/argocd-image-updater/values-dev.yaml -n argo --install --kubeconfig **** --wait --timeout=10m --set 'config.registries[0].credentials=env:DOCKER_HUB_CREDS=:'
  ++ helm upgrade argocd-image-updater ./ground/cicd-server/argo/helm/argocd-image-updater -f ./ground/cicd-server/argo/helm/argocd-image-updater/values-dev.yaml -n argo --install --kubeconfig **** --wait --timeout=10m --set 'config.registries[0].credentials=env:DOCKER_HUB_CREDS=:'
  Error: UPGRADE FAILED: another operation (install/upgrade/rollback) is in progress
  [Pipeline] }
  [Pipeline] // withCredentials
  [Pipeline] }
  [Pipeline] // script
  [Pipeline] }
  [Pipeline] // stage
  [Pipeline] }
  [Pipeline] // withEnv
  [Pipeline] }
  [Pipeline] // withEnv
  [Pipeline] }
  [Pipeline] // node
  [Pipeline] End of Pipeline
  ERROR: script returned exit code 1
  Finished: FAILURE
  ```

    - 뭐 작업중인 것이 있어 마무리먼저 하라고 한다.
    - 작업중인 내역 확인

        ```bash
        [jenkins@cicd-server .kube]$ helm history argocd-image-updater --namespace argo
        REVISION        UPDATED                         STATUS          CHART                           APP VERSION DESCRIPTION
        1               Sat Sep 21 12:22:39 2024        pending-install argocd-image-updater-0.9.2      v0.12.2     Initial install underway
        
        ```

        - uninstall 해보자

          <img src="https://github.com/user-attachments/assets/65692e45-5ebb-4e57-be0b-3dfa2a50e22b" alt="adder" width="30%" />

          <img src="https://github.com/user-attachments/assets/f41ae070-c489-4dbb-bf57-4fc3b02e961f" alt="adder" width="20%" />

- 3) calico error - 1

jenkins pipeline으로  `argocd-image-updater` 를 제거 후 다시 설치해도 안됐다.

일단 어설프게 생성됐었던 Pod들이 있어서 삭제 요청을 했더니 이벤트에 아래와 같이 나왔다.

> error killing pod: failed to "KillPodSandbox" for "270f7734-2e29-4d2c-9de3-c83fbe80a3ad" with KillPodSandboxError: "rpc error: code = Unknown desc = failed to destroy network for sandbox \"e03127820f46f10228f539934cf3a5622038221bb3cca3e1b2d42b79f492f611\": plugin type=\"calico\" failed (delete): error getting ClusterInformation: connection is unauthorized: Unauthorized”
>

calico.. 인증…? 일단 calico 관련 pod들의 로그를 살펴보자

    - `kubectl logs -n calico-system -l k8s-app=calico-node`

      > Defaulted container "calico-node" out of: calico-node, flexvol-driver (init), install-cni (init)
      2024-09-30 16:17:19.854 [INFO][2278] felix/int_dataplane.go 1325: Linux interface addrs changed. addrs=set.Set{fe80::a00:27ff:feec:1d63,192.168.56.30} ifaceName="eth1"
      2024-09-30 16:17:19.854 [INFO][2278] felix/int_dataplane.go 1913: Received interface addresses update msg=&intdataplane.ifaceAddrsUpdate{Name:"eth1", Addrs:set.Typed[string]{"192.168.56.30":set.v{}, "fe80::a00:27ff:feec:1d63":set.v{}}}
      2024-09-30 16:17:19.854 [INFO][2278] felix/hostip_mgr.go 84: Interface addrs changed. update=&intdataplane.ifaceAddrsUpdate{Name:"eth1", Addrs:set.Typed[string]{"192.168.56.30":set.v{}, "fe80::a00:27ff:feec:1d63":set.v{}}}
      2024-09-30 16:17:19.854 [INFO][2278] felix/ipsets.go 130: Queueing IP set for creation family="inet" setID="this-host" setType="hash:ip"
      2024-09-30 16:17:19.857 [INFO][2278] felix/ipsets.go 778: Doing full IP set rewrite family="inet" numMembersInPendingReplace=5 setID="this-host"
      2024-09-30 16:18:21.230 [INFO][2278] felix/summary.go 100: Summarising 14 dataplane reconciliation loops over 1m3.5s: avg=21ms longest=92ms ()
      2024-09-30 16:19:23.571 [INFO][2278] felix/summary.go 100: Summarising 10 dataplane reconciliation loops over 1m2.3s: avg=13ms longest=79ms ()
      2024-09-30 16:20:26.615 [INFO][2278] felix/summary.go 100: Summarising 9 dataplane reconciliation loops over 1m3s: avg=5ms longest=9ms (resync-mangle-v4)
      2024-09-30 16:21:30.214 [INFO][2278] felix/summary.go 100: Summarising 11 dataplane reconciliation loops over 1m3.6s: avg=15ms longest=94ms ()
      2024-09-30 16:22:31.132 [INFO][2278] felix/summary.go 100: Summarising 10 dataplane reconciliation loops over 1m0.9s: avg=18ms longest=75ms ()
      >
        - 별다른 에러가 없는것 같다…
    - `kubectl logs -n calico-system -l k8s-app=calico-kube-controllers`

      > Trace[1727128170]: ---"Objects listed" error:<nil> 14770ms (12:24:29.673)
      Trace[1727128170]: [14.770813912s] [14.770813912s] END
      I0924 12:24:29.698241       1 trace.go:219] Trace[1675605975]: "Reflector ListAndWatch" name:pkg/mod/k8s.io/client-go@v0.26.8/tools/cache/reflector.go:169 (24-Sep-2024 12:24:14.903) (total time: 14795ms):
      Trace[1675605975]: ---"Objects listed" error:<nil> 14795ms (12:24:29.698)
      Trace[1675605975]: [14.795151925s] [14.795151925s] END
      2024-09-24 12:24:30.081 [ERROR][1] main.go 297: Received bad status code from apiserver error=an error on the server ("[+]ping ok\n[+]log ok\n[-]etcd failed: reason withheld\n[+]poststarthook/start-kube-apiserver-admission-initializer ok\n[+]poststarthook/generic-apiserver-start-informers ok\n[+]poststarthook/priority-and-fairness-config-consumer ok\n[+]poststarthook/priority-and-fairness-filter ok\n[+]poststarthook/storage-object-count-tracker-hook ok\n[+]poststarthook/start-apiextensions-informers ok\n[+]poststarthook/start-apiextensions-controllers ok\n[+]poststarthook/crd-informer-synced ok\n[+]poststarthook/start-system-namespaces-controller ok\n[+]poststarthook/bootstrap-controller ok\n[+]poststarthook/rbac/bootstrap-roles ok\n[+]poststarthook/scheduling/bootstrap-system-priority-classes ok\n[+]poststarthook/priority-and-fairness-config-producer ok\n[+]poststarthook/start-cluster-authentication-info-controller ok\n[+]poststarthook/start-kube-apiserver-identity-lease-controller ok\n[+]poststarthook/start-deprecated-kube-apiserver-identity-lease-garbage-collector ok\n[+]poststarthook/start-kube-apiserver-identity-lease-garbage-collector ok\n[+]poststarthook/start-legacy-token-tracking-controller ok\n[+]poststarthook/aggregator-reload-proxy-client-cert ok\n[+]poststarthook/start-kube-aggregator-informers ok\n[+]poststarthook/apiservice-registration-controller ok\n[+]poststarthook/apiservice-status-available-controller ok\n[+]poststarthook/kube-apiserver-autoregistration ok\n[+]autoregister-completion ok\n[+]poststarthook/apiservice-openapi-controller ok\n[+]poststarthook/apiservice-openapiv3-controller ok\n[+]poststarthook/apiservice-discovery-controller ok\nhealthz check failed") has prevented the request from succeeding status=500
      2024-09-24 12:24:30.274 [INFO][1] main.go 313: Health check is not ready, retrying in 2 seconds with new timeout: 16s
      2024-09-24 12:24:46.950 [INFO][1] resources.go 350: Main client watcher loop
      2024-09-24 12:26:41.369 [INFO][1] ipam_allocation.go 175: Candidate IP leak handle="k8s-pod-network.545973fd2ff794d574c1562600514d7e4b573c1773fb205722fe81d02227d8ff" ip="20.96.235.199" node="k8s-master" pod="default/app-1-2-2-1-78cbbff668-8s94x"
      2024-09-24 12:31:16.997 [INFO][1] ipam_allocation.go 175: Candidate IP leak handle="k8s-pod-network.7e0068785853cda7e6e45f9a7a01e24febe778600a7543acabccea934dcdd694" ip="20.96.235.218" node="k8s-master" pod="anotherclass-223/api-tester-2231-787c7b8499-mvcmk"
      >
        - 문제가 있다.. `Received bad status code from apiserver error` 가 눈에 띈다.
    - kube-apiserver 로그

      > E0930 16:27:41.194702       1 queueset.go:483] "Overflow" err="queueset::currentR overflow" QS="workload-low" when="2024-09-30 16:27:41.194650714" prevR="13136.81459206ss" incrR="184467440737.09551136ss" currentR="13136.81458726ss"
      E0930 16:27:41.197237       1 queueset.go:483] "Overflow" err="queueset::currentR overflow" QS="workload-low" when="2024-09-30 16:27:41.197225205" prevR="13136.81744731ss" incrR="184467440737.09548702ss" currentR="13136.81741817ss"
      E0930 16:27:41.203534       1 queueset.go:483] "Overflow" err="queueset::currentR overflow" QS="workload-low" when="2024-09-30 16:27:41.203521038" prevR="13136.82471064ss" incrR="184467440737.09547061ss" currentR="13136.82466509ss"
      E0930 16:27:41.203583       1 queueset.go:483] "Overflow" err="queueset::currentR overflow" QS="workload-low" when="2024-09-30 16:27:41.203578152" prevR="13136.82474769ss" incrR="184467440737.09550719ss" currentR="13136.82473872ss"
      E0930 16:27:46.175827       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:27:47.169529       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:27:53.377656       1 queueset.go:483] "Overflow" err="queueset::currentR overflow" QS="workload-low" when="2024-09-30 16:27:53.377608573" prevR="13137.01156310ss" incrR="184467440737.09548673ss" currentR="13137.01153367ss"
      E0930 16:27:59.181547       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:27:59.190429       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:28:04.117755       1 queueset.go:483] "Overflow" err="queueset::currentR overflow" QS="leader-election" when="2024-09-30 16:28:04.117735438" prevR="4317.59327287ss" incrR="184467440737.09551186ss" currentR="4317.59326857ss"
      I0930 16:28:09.659695       1 trace.go:219] Trace[1676232599]: "Get" accept:application/vnd.kubernetes.protobuf, */*,audit-id:f90b20ec-8998-4457-bdb2-3ccd52c6503f,client:20.96.235.204,protocol:HTTP/2.0,resource:pods,scope:resource,url:/api/v1/namespaces/kube-system/pods/etcd-k8s-master/log,user-agent:dashboard/v2.7.0,verb:CONNECT (30-Sep-2024 16:28:08.805) (total time: 854ms):
      Trace[1676232599]: ---"Writing http response done" 851ms (16:28:09.659)
      Trace[1676232599]: [854.250778ms] [854.250778ms] END
      E0930 16:28:13.184350       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:28:13.185508       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:28:25.862703       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:28:25.869581       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:28:34.506918       1 queueset.go:483] "Overflow" err="queueset::currentR overflow" QS="workload-low" when="2024-09-30 16:28:34.506901045" prevR="13137.66760901ss" incrR="184467440737.09549623ss" currentR="13137.66758908ss"
      E0930 16:28:36.171449       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      E0930 16:28:37.189871       1 authentication.go:73] "Unable to authenticate the request" err="[invalid bearer token, service account token has expired]"
      >
        - 두 가지 문제가 계속해서 발생하고 있어보인다.
            - Bearer Token 인증 실패
            - QueueSet Overflow
            - 인증이 실패하며 요청이 계속 큐에 쌓이고 있는 상황으로 보인다…. 메모리 사용량이 계속 떨어지지 않았던게 이거때문인가… 음.. 이럴때는 일단 컴퓨터를 재부팅하자.
        - 재부팅하니까 이 로그는 더이상 발생하지는 않았다.
- 4) calico error - 2

이번에는 아래와 같은 에러가 `argocd-image-updater` 디플로이먼트에 찍혔다.

  ```bash
  Failed to create pod sandbox: rpc error: code = Unknown desc = failed to setup network for sandbox "c073fc867588054f1ee2dafbb3926708d233e46d1482a4776f34cde3d94c11a5": plugin type="calico" failed (add): error getting ClusterInformation: connection is unauthorized: Unauthorized
  ```

    - 또.. calico 인증…? kube-apiserver에는 그런 로그는 이제 찍히지 않고 있다. 이제는 진짜 인증 문제인것 같은데… 나는 저런걸 만진 적이 없다.. 어떤 글에서 calico node를 재시작하면 인증서를 재발급받아 문제가 해결될수도 있다고 한다.  calico node를 재시작해보자.

        ```bash
        kubectl delete -n calico-system pod calico-node-jpjqt
        ```

    - 아오 이제 된다….

### 에러 원인 추측

- 인증서는 만진적이 없는데… 만료라니…. 일단 만료는 아무래도 시간이 지난 경우일텐데,,, 불현듯 뭐 하나가 떠올랐다.
- k8s master node가 계속 `timedatectl` 이 싱크가 안맞는 경우가 있다. 중간에 잠깐 이 싱크를 맞춰줬었다. 그랬더니 6일이라는 시간이 지나있었다.
- 아마 이 이유 때문이지 않을까 싶다….

### (1) Image Updater 세팅

- Pod 로그 확인

    ```bash
    ...
    time="2024-09-30T17:00:02Z" level=info msg="Starting image update cycle, considering 0 annotated application(s) for update"
    time="2024-09-30T17:00:02Z" level=info msg="Processing results: applications=0 images_considered=0 images_skipped=0 images_updated=0 errors=0"
    time="2024-09-30T17:02:02Z" level=info msg="Starting image update cycle, considering 0 annotated application(s) for update"
    time="2024-09-30T17:02:02Z" level=info msg="Processing results: applications=0 images_considered=0 images_skipped=0 images_updated=0 errors=0"
    time="2024-09-30T17:04:02Z" level=info msg="Starting image update cycle, considering 0 annotated application(s) for update"
    ...
    ```

  감지할 것을 세팅하지 않았기 때문에 아무내용도 나오지 않고 있다.

- Annotation 세팅
    - Argo CD에 등록한 App - `Details` - `Summary` - `Edit`
        - Annotation key - value

            ```bash
            // 도커 허브에서 이미지 대상 지정
            // argocd-image-updater.argoproj.io/image-list=<alias>=<Docker Username>/<Image Name>
            argocd-image-updater.argoproj.io/image-list=1pro-api-tester=1pro/api-tester
            
            // 업데이트 전략 선택
            // argocd-image-updater.argoproj.io/<alias>.update-strategy=name
            argocd-image-updater.argoproj.io/1pro-api-tester.update-strategy=name
            
            // 태그 정규식 설정 (1.1.1-231220.175735) 
            argocd-image-updater.argoproj.io/1pro-api-tester.allow-tags=regexp:^1.1.1-[0-9]{6}.[0-9]{6}$
            ```

          ![image.png](https://github.com/user-attachments/assets/76a551af-d981-4b54-8430-eb7441a03488)

            - 업데이트 전략(**update-strategy)**
                - **semver :** 주어진 이미지 제약 조건에 따라 허용되는 가장 높은 버전으로 업데이트
                - **latest :** 가장 최근에 생성된 이미지 태그로 업데이트
                - **name :** 알파벳순으로 정렬된 목록의 마지막 태그로 업데이트
                - **digest :** 변경 가능한 태그의 최신 푸시 버전으로 업데이트

              **▶** 이미지 태그(tag) 업데이트 방식 설정 : https://argocd-image-updater.readthedocs.io/en/stable/basics/update-strategies/

- Sync Policy
    - **PRUNE RESOURCES :** Git에서 리소스 삭제시 실제 Kubernetes에서도 자원이 삭제됨
    - **SELF HEAL :** Auto Sync 상태에서 항상 Git에 있는 내용이 적용됨 (이때 ArgoCD나 Kuberentes에서 직접 수정한 내용은 삭제됨)
- 수정된 App

  <img src="https://github.com/user-attachments/assets/5120ae14-4478-4e32-b250-bc7c5241dbf5" alt="adder" width="100%" />


## 2) Image Updater의 이미지 감지

```bash
time="2024-09-30T17:14:04Z" level=info msg="Setting new image to 1pro/api-tester:1.1.1-240125.251340" alias=1pro-api-tester application=api-tester-2232 image_name=1pro/api-tester image_tag=1.0.0 registry=
time="2024-09-30T17:14:04Z" level=info msg="Successfully updated image '1pro/api-tester:1.0.0' to '1pro/api-tester:1.1.1-240125.251340', but pending spec update (dry run=false)" alias=1pro-api-tester application=api-tester-2232 image_name=1pro/api-tester image_tag=1.0.0 registry=
time="2024-09-30T17:14:04Z" level=info msg="Committing 1 parameter update(s) for application api-tester-2232" application=api-tester-2232
time="2024-09-30T17:14:04Z" level=info msg="Successfully updated the live application spec" application=api-tester-2232
time="2024-09-30T17:14:04Z" level=info msg="Processing results: applications=1 images_considered=1 images_skipped=0 images_updated=1 errors=0"
time="2024-09-30T17:16:05Z" level=info msg="Starting image update cycle, considering 1 annotated application(s) for update"
time="2024-09-30T17:16:06Z" level=info msg="Processing results: applications=1 images_considered=1 images_skipped=0 images_updated=0 errors=0"
```

이미지를 감지하기 시작했다.

## 3) 이미지 빌드 및 자동 배포

이미지를 빌드하니 Image Updater가 변경을 감지했다.

```bash
2232 image_name=1pro/api-tester image_tag=1.0.0 registry=
time="2024-09-30T17:14:04Z" level=info msg="Successfully updated image '1pro/api-tester:1.0.0' to '1pro/api-tester:1.1.1-240125.251340', but pending spec update (dry run=false)" alias=1pro-api-tester application=api-tester-2232 image_name=1pro/api-tester image_tag=1.0.0 registry=
time="2024-09-30T17:14:04Z" level=info msg="Committing 1 parameter update(s) for application api-tester-2232" application=api-tester-2232
time="2024-09-30T17:14:04Z" level=info msg="Successfully updated the live application spec" application=api-tester-2232
time="2024-09-30T17:14:04Z" level=info msg="Processing results: applications=1 images_considered=1 images_skipped=0 images_updated=1 errors=0"
time="2024-09-30T17:16:05Z" level=info msg="Starting image update cycle, considering 1 annotated application(s) for update"
```

<img src="https://github.com/user-attachments/assets/9c80c2c0-66e5-4b8f-9b16-0ee5b84b0f7a" alt="adder" width="60%" />

auto sync가 적용이 안되었다…

<img src="https://github.com/user-attachments/assets/d04db4a3-029e-47fd-a4ae-1e57b9b546b7" alt="adder" width="100%" />

Argo CD에서 변경내용을 확인했고, 새로 배포가 진행됐다.

# 5. Argo Rollouts 를 이용해서 Blue-Green 배포하기

## 1) Jenkins Pipeline으로 Argo Rollouts 설치

- 기존 Jenkins Pipeline으로 동일하게 진행

### values-dev.yaml 파일

```yaml
controller:
  replicas: 1

dashboard:
  enabled: true
  service:
    type: NodePort
    portName: dashboard
    port: 3100
    targetPort: 3100
    nodePort: 30003
```

- `dashboard.enabled` - ArgoCD 전용 Dashboard가 있는데, 그걸 사용하기 위함.
    - `dashboard.service.nodePort` - 포트를 할당하여 웹에서 접근 가능하도록 설정.

### Pods 생성 확인

- deployment가 만들지 않고 `rollout controller`가 `ReplicaSet`을 만들고 `Pod`를 생성하게 된다.

  <img src="https://github.com/user-attachments/assets/b58b02e0-1c5d-4aa9-a0be-6102ab91fda1" alt="adder" width="100%" />


## 2) Argo 로 App 생성 및 Blue-Green 배포하기

### (1) App 생성시 사용한 yaml

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: api-tester-2233
spec:
  destination:
    name: ''
    namespace: anotherclass-223
    server: 'https://kubernetes.default.svc'
  source:
    path: 2233/deploy/argo-rollouts
    repoURL: 'https://github.com/jiyongYoon/kubernetes-anotherclass-sprint2.git'
    targetRevision: main
  sources: []
  project: default
```

### (2) 배포한 repository 내용

<img src="https://github.com/user-attachments/assets/96b9b9f8-b2dd-445e-8e0d-d04ec905fc38" alt="adder" width="30%" />

- service-active.yaml / servaice-preview.yaml

    ```yaml
    apiVersion: v1
    kind: Service
    metadata:
      name: api-tester-2233-active / api-tester-2233-preview   <-- 이 부분만 다
      labels:
        part-of: k8s-anotherclass
        component: backend-server
        name: api-tester
        instance: api-tester-2233
        version: 1.0.0
        managed-by: kubectl
    spec:
      selector:
        part-of: k8s-anotherclass
        component: backend-server
        name: api-tester
        instance: api-tester-2233
      ports:
        - port: 80
          targetPort: http
          nodePort: 32233
      type: NodePort
    ```

- rollout.yaml

    ```yaml
    apiVersion: argoproj.io/v1alpha1
    kind: Rollout
    metadata:
      name: api-tester-2233
      labels:
        part-of: k8s-anotherclass
        component: backend-server
        name: api-tester
        instance: api-tester-2233
        version: 1.0.0
        managed-by: kubectl
    spec:
      replicas: 2
      strategy:
        blueGreen:   <<--- deployment.yaml 에서 다른 부분은 이 부분이 전부!!
          activeService: api-tester-2233-active
          previewService: api-tester-2233-preview
          autoPromotionEnabled: false
      selector:
        matchLabels:
          part-of: k8s-anotherclass
          component: backend-server
          name: api-tester
          instance: api-tester-2233
      template:
        metadata:
          labels:
            part-of: k8s-anotherclass
            component: backend-server
            name: api-tester
            instance: api-tester-2233
            version: 1.0.0
        spec:
          nodeSelector:
            kubernetes.io/hostname: k8s-master
          containers:
            - name: api-tester-2233
              image: 1pro/api-tester:1.0.0
              imagePullPolicy: Always
              ports:
                - containerPort: 8080
                  name: http
              envFrom:
                - configMapRef:
                    name: api-tester-2233-properties
              startupProbe:
                httpGet:
                  path: "/startup"
                  port: 8080
                periodSeconds: 5
                failureThreshold: 24
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
                - name: secret-datasource
                  mountPath: /usr/src/myapp/datasource
          volumes:
            - name: secret-datasource
              secret:
                secretName: api-tester-2233-postgresql
    ```

    - `strategy.blueGreen.autoPromotionEnabled` - blue → green 배포 시 배포가 된 후 `Promote`를 누르지 않아도 자동으로 트래픽이 전환되도록 할지 말지 설정하는 부분.
        - `true` - default. 자동으로 트래픽이 전환되며 이전 버전은 삭제됨
        - `false` - `Promote` 동작 전에는 `previewService` 로 트래픽만 전환되어 있음.
        - [추가 설정들](https://argo-rollouts.readthedocs.io/en/stable/features/bluegreen/)

### (3) blue - green 업데이트 배포

- git에서 새로운 배포버전으로 태그 수정 후 `ArgoCD` - `App` - `Refresh`
    - 변경사항 있음 확인

      <img src="https://github.com/user-attachments/assets/155bdb6b-e6c8-4acb-9786-6072c6c1c3b4" alt="adder" width="60%" />

- `Sync`로 업데이트 버전 배포

  <img src="https://github.com/user-attachments/assets/e6c5d0f2-ad4e-4871-b034-c686e82ba5e8" alt="adder" width="60%" />

    - preview-service(왼쪽) 은 호출이 끊어짐

      <img src="https://github.com/user-attachments/assets/28f8b7f2-5697-46b3-8532-4970485934c3" alt="adder" width="50%" />

    - 컴퓨터가 힘들어하다가….

      <img src="https://github.com/user-attachments/assets/5bf4301d-0700-4d1f-8a18-3a7be0254635" alt="adder" width="50%" />

      <img src="https://github.com/user-attachments/assets/215c0e62-4ff1-47e6-bd59-dbdf9a173a6b" alt="adder" width="50%" />

    - 자기가 회복함…

      <img src="https://github.com/user-attachments/assets/10bd2ba7-9940-4e46-99ec-c7e2c061bff8" alt="adder" width="50%" />

      <img src="https://github.com/user-attachments/assets/87c0a1d7-3d8c-46a4-8ffe-1912c99b18ab" alt="adder" width="50%" />

- 새 배포버전 Pod의 정보 확인

  <img src="https://github.com/user-attachments/assets/4281199f-21c3-4d8f-814c-ef389c73b2a8" alt="adder" width="50%" />

    - `rollouts-pod-template-hash`에 난수 생성
- preview-service 정보 확인

  <img src="https://github.com/user-attachments/assets/ac3fdcfe-00c2-48a9-9641-d760bbd0d4e4" alt="adder" width="50%" />

- Rollout Pause 중임 확인

  <img src="https://github.com/user-attachments/assets/ac278430-ec02-48f2-8254-ba77e12fd4d6" alt="adder" width="50%" />

  <img src="https://github.com/user-attachments/assets/9e5928df-62f0-480d-be96-41c4cd675e76" alt="adder" width="50%" />

    - ArgoCD의 Rollouts를 설치하면 ArgoCD에서 버튼이 활성화 되는 부분!

  > **[중요!!]**
  > 
  > 이 부분은 그 전에도 말했듯이, ArgoCD를 사용하지 않더라도 Rollouts만 설치하면 Rollouts Dashboard에서 컨트롤도 가능하다!!!

### (4) Argo Rollout Dashboard

<img src="https://github.com/user-attachments/assets/4b47cd58-a774-4820-9326-fa4027939ad7" alt="adder" width="100%" />

- Restart
    - Revision 1, 2 각각 pod를 1개씩 재생성시킴 → 굳이 사용할 일이 거의 없음. 만약 새로 업데이트한 Pod들을 재생성하고 싶으면 쿠버네티스에서 직접 삭제를 해주어 재생성시키면 됨
- Promote
    - 다음 단계로 넘어감
    - blue-green 배포시에는 업데이트 된 pod로 트래픽을 전환시키고, 기존 pod들은 30초 후에 삭제함. (rollout.yaml에서 30초 옵션은 설정으로 변경 가능)

      <img src="https://github.com/user-attachments/assets/4d2e9b8d-9a2b-48b3-acba-8acb20d59ec0" alt="adder" width="70%" />

        - 30초 후 삭제중

          <img src="https://github.com/user-attachments/assets/5449a22b-ddbd-4a3d-9f46-d4d194d12b2c" alt="adder" width="70%" />

        - 없어짐

          <img src="https://github.com/user-attachments/assets/c8411fb8-ee39-46b0-b3f1-5a9e9f236af8" alt="adder" width="70%" />

    - 기존 pod들은 삭제됨

      <img src="https://github.com/user-attachments/assets/3ad39adb-cb7d-4e2f-9700-c5ff631d16fb" alt="adder" width="70%" />

        - replicaSet만 남아있
- Promotefull
    - 모든 단계를 넘어감 - canary 배포 시에는 단계가 여러개 있어서 사용하기도 함

### (5) Argo Rollout CLI

- CLI로도 사용이 가능
    - 상세 내용

      **▶** Argo Rollouts CLI 설치 (v1.6.4)

        ```bash
        # root 계정
        curl -LO https://github.com/argoproj/argo-rollouts/releases/download/v1.6.4/kubectl-argo-rollouts-linux-amd64
        chmod +x ./kubectl-argo-rollouts-linux-amd64
        mv ./kubectl-argo-rollouts-linux-amd64 /usr/local/bin/kubectl-argo-rollouts
        
        # 설치 확인
        [root@k8s-master ~]# kubectl argo rollouts version
        kubectl-argo-rollouts: v1.6.4+a312af9
          BuildDate: 2023-12-11T18:31:15Z
          GitCommit: a312af9f632b985ec13f64918b918c5dcd02a15e
          GitTreeState: clean
          GoVersion: go1.20.12
          Compiler: gc
          Platform: linux/amd64
        ```

      **▶** 조회하기

        ```bash
        # Rollout 조회 하기
        kubectl argo rollouts get rollout api-tester-2233 -n anotherclass-223 -w
        ```

      [Rollouts - Argo Rollouts - Kubernetes Progressive Delivery Controller](https://argo-rollouts.readthedocs.io/en/stable/generated/kubectl-argo-rollouts/kubectl-argo-rollouts/)


# 6. Argo Rollouts 를 이용해서 Canary 배포하기

## 1) Argo Rollouts 설치

- 위에서 작업 진행함

## 2) Kubectl로 Rollouts 배포하기

### (1) 배포

```bash
[root@k8s-master ~]#
kubectl apply -f https://raw.githubusercontent.com/k8s-1pro/kubernetes-anotherclass-sprint2/main/2234/deploy/argo-rollouts/rollout.yaml -n anotherclass-223
kubectl apply -f https://raw.githubusercontent.com/k8s-1pro/kubernetes-anotherclass-sprint2/main/2234/deploy/argo-rollouts/configmap.yaml -n anotherclass-223
kubectl apply -f https://raw.githubusercontent.com/k8s-1pro/kubernetes-anotherclass-sprint2/main/2234/deploy/argo-rollouts/secret.yaml -n anotherclass-223
kubectl apply -f https://raw.githubusercontent.com/k8s-1pro/kubernetes-anotherclass-sprint2/main/2234/deploy/argo-rollouts/service.yaml -n anotherclass-223
```

- rollout.yaml

    ```yaml
    apiVersion: argoproj.io/v1alpha1
    kind: Rollout
    metadata:
      name: api-tester-2234
      labels:
        part-of: k8s-anotherclass
        component: backend-server
        name: api-tester
        instance: api-tester-2234
        version: 1.0.0
        managed-by: kubectl
    spec:
      replicas: 2
      strategy:
        canary:   <<<--- 이 부분이 canary 배포 전략
          steps:
            - setWeight: 33
            - pause: {}
            - setWeight: 66
            - pause: { duration: 2m }
      selector:
        matchLabels:
          part-of: k8s-anotherclass
          component: backend-server
          name: api-tester
          instance: api-tester-2234
      template:
        metadata:
          labels:
            part-of: k8s-anotherclass
            component: backend-server
            name: api-tester
            instance: api-tester-2234
            version: 1.0.0
        spec:
          nodeSelector:
            kubernetes.io/hostname: k8s-master
          containers:
            - name: api-tester-2234
              image: 1pro/api-tester:1.0.0
              imagePullPolicy: Always
              ports:
                - containerPort: 8080
                  name: http
              envFrom:
                - configMapRef:
                    name: api-tester-2234-properties
              startupProbe:
                httpGet:
                  path: "/startup"
                  port: 8080
                periodSeconds: 5
                failureThreshold: 24
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
                - name: secret-datasource
                  mountPath: /usr/src/myapp/datasource
          volumes:
            - name: secret-datasource
              secret:
                secretName: api-tester-2234-postgresql
    ```


### (2) 배포 모니터링

```bash
# Rollout 조회 하기
kubectl argo rollouts get rollout api-tester-2234 -n anotherclass-223 -w
```

<img src="https://github.com/user-attachments/assets/21a811bb-bafe-4edb-a75f-dbe5e38bbaa6" alt="adder" width="70%" />

- argo rollout dashboard

  <img src="https://github.com/user-attachments/assets/fcbdba91-0367-4a85-8652-6e802e326ef7" alt="adder" width="80%" />


### (3) 트래픽 확인하기

```bash
[root@k8s-master ~]# while true; do curl http://192.168.56.30:32234/version; sleep 2; echo '';  done;
[App Version] : Api Tester v1.0.0
[App Version] : Api Tester v1.0.0
```

## 3) canary 배포 시작하기 (CLI로)

```bash
# kubectl argo rollouts edit <ROLLOUT_NAME> -n <NAMESPACE_NAME>
kubectl argo rollouts edit api-tester-2234 -n anotherclass-223

# kubectl argo rollouts set image <ROLLOUT_NAME> <CONTAINER_NAME>=<IMAGE>:<TAG> -n <NAMESPACE_NAME>
kubectl argo rollouts set image api-tester-2234 api-tester-2234=1pro/api-tester:2.0.0 -n anotherclass-223
```

- 위는 rollout에 직접 들어가서 tag를 변경하는 명령어
- 아래는 이미지를 변경하는 명령어 (좀 더 안전하겠네?)
- `step1` - 33% 배포

  <img src="https://github.com/user-attachments/assets/82ad1bc5-d3bf-47e8-a288-032b4f199356" alt="adder" width="80%" />

  <img src="https://github.com/user-attachments/assets/02a1ff5b-1874-4b33-92ef-cb827f9216d3" alt="adder" width="40%" />

- `step2` - pause → promote

  <img src="https://github.com/user-attachments/assets/373d1d7b-737d-49e1-95be-8ef7af3a3635" alt="adder" width="80%" />

- `step3` - 66% 배포

  <img src="https://github.com/user-attachments/assets/15750fc4-462b-432e-b583-728a7b2c978a" alt="adder" width="80%" />

  <img src="https://github.com/user-attachments/assets/6ab18b34-34f2-4f69-b3ad-924d83846eb3" alt="adder" width="40%" />

- `step4` - 2분 후 canary 배포 완료

  <img src="https://github.com/user-attachments/assets/09421353-67a6-437f-989d-dc5c2d097e39" alt="adder" width="80%" />

  <img src="https://github.com/user-attachments/assets/01cc3406-e574-4461-8e9b-d82232a6637d" alt="adder" width="80%" />

  <img src="https://github.com/user-attachments/assets/5de55119-1804-4beb-8745-6c6e2bdfdac5" alt="adder" width="40%" />