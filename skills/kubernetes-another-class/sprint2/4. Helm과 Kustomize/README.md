# 4. Helm과 Kustomize

---

# 1. Helm과 Kustomize 개요

프로그램을 배포할 때는 앱별 / 환경별 배포를 해야하는 경우가 생긴다.

내게 좀 더 익숙한 Docker 배포 환경으로 생각한다면, Springboot 서버와 Nginx, DB 서버를 띄워야 하며(앱별) , 해당 서버들을 개발환경, staging 환경, production 환경(환경별) 등으로 나누어 띄워야 한다.

이 경우, 앱별로 dockerfile에 변수를 받을 수 있도록 세팅하고, docker-compose 파일을 환경별로 구성하여 배포를 진행하게 된다.

kubernetes로 진행할 경우, 앱별로 Pod 배포를 위한 yaml 파일들이 있고, 이 yaml 파일들을 각 환경별로 나누어 관리해야 한다.

이 yaml 파일을 CLI로 배포하기 위해서는 각 yaml 파일을 etcd에 등록하는 kubectl을 개발자가 직접 날려야 한다. (물론 이 과정을 Jenkins Pipeline으로 관리할 수도 있기는 하다)

이러한 앱별 / 환경별 배포를 관리하기 위한 도구가 바로 Helm과 Kustomize다. 따라서 이 두 툴은 배포툴이라기보다는 `패키지 매니저`라고 부르는 것이 더 정확한 역할이다.

배포할 App을 패기지 단위로 구분하여 관리하게 되는 것이다.

# 2. Helm과 Kustomize 비교

- kubernetes 생태계를 제대로 활용하기 위해서는 결국 Helm의 사용은 필수적이 된다. 따라서 Kustomize는 어떤 툴인지 확인하는 정도로 학습하고 넘어가며, 이번에는 Helm만 구체적으로 학습하고자 한다.
- 가장 큰 차이점은 `한 패키지 당 활용 범위`라고 한다. (강의자의 의견이라, 학습하며 경험해보려고 한다.)

    | **툴** | **활용 범위**                                                                                   |
    |---------------------------------------------------------------------------------------------| --- |
    | helm | `마이크로 서비스` + `다양한 배포 환경` 양 쪽 모두에 함께 적용해도 무방                                                 |
    | kustomize | `마이크로 서비스` + `다양한 배포 환경` 중 하나의 목적만 가지고 활용하는 것이 좋음 (이유 - 내부 패키지의 파일 갯수가 너무 많아져 구조가 복잡해지기 때문) |
    

## Helm과 Kustomize 설치방법 및 구성도

<img src="https://github.com/user-attachments/assets/ed0bbb68-4375-4420-9d2a-1a93930f9c13" alt="adder" width="100%" />

### 설치

- helm
    - helm 공식 repo에서 다운로드 받아서 인스톨받아서 사용한다.
- kustomize
    - kubectl v1.14 부터 kustomize 기능이 통합되어 배포되고 있다.

### 사용

- helm
    - helm command를 사용하면 kubernetes 클러스터의 kube-apiserver에 명령이 전달된다. 이를 위해 kubernetes를 설치할 때 발급된 `인증서`가 반드시 필요하다.
    - 기타 서비스들은 각 서비스별로 helm 설치 가이드를 제공하는데, `Artifact HUB`라는 Helm 패키지 저장소를 사용하게 된다. (DockerHub처럼)
- kustomize
    - kubectl과 기능이 통합되어 배포되고 있기 때문에 기본 command는 kubectl에 `-k` 옵션을 주면 kustomize 명령어가 된다. 역시 kubectl 사용때처럼 kubernetes `인증서`가 필요하다.

# 3. Helm 설치 및 사용

## 1) Helm 설치

**Helm**(Ver. 3.13.2) **설치**

- Download : https://get.helm.sh/helm-v3.13.2-linux-amd64.tar.gz
- Site : https://helm.sh/docs/intro/install/
- Release : https://github.com/helm/helm/releases

### 인텔 아키텍처용

```bash
curl -O https://get.helm.sh/helm-v3.13.2-linux-amd64.tar.gz

tar -zxvf helm-v3.13.2-linux-amd64.tar.gz

mv linux-amd64/helm /usr/bin/helm
```

- ci/cd 서버에 root 유저로 설치한다

### Helm 설치 확인

```bash
# jenkins 유저로 전환해서 확인
[root@cicd-server ~]# su - jenkins -s /bin/bash
[jenkins@cicd-server ~]$ helm
```

## 2) Helm 템플릿 생성

- 템플릿은 `차트` 라고 부르기도 함.

### Helm 템플릿 생성

```bash
[jenkins@cicd-server ~]$ helm create api-tester
```

### 템플릿 기본 구성

<img src="https://github.com/user-attachments/assets/53bdc7a8-60ac-44d7-9473-a07e6dbf1d14" alt="adder" width="50%" />

- `api-tester` - Main 차트 폴더
    - `charts` - Sub 차트 폴더: Main App 배포를 위한 Sub App을 배포하기 위한 폴더 (사진에는 없으나 만들어 지는 경우가 있음)
    - `templates` - Main 차트의 yaml 파일 폴더
        - `tests` - App의 통신 상태 확인을 위한 helm의 부가기능 yaml
        - `_helpers.tpl` - 사용자 정의 전역변수 선언
        - `deployment.yaml` - 배포 될 deployment.yaml 파일
        - `hpa.yaml` - 배포 될 hpa.yaml 파일
        - `ingress.yaml` - 배포 될 ingress.yaml 파일
        - `NOTES.txt` - 배포 후 안내 문구
        - `service.yaml` - 배포 될 service.yaml 파일
        - `serviceaccount.yaml` - 배포 될 serviceaccount.yaml 파일
        - 이 후 필요한 파일들은 추가로 생성 (e.g. `configmap.yaml`, `secret.yaml` 등)
    - `.helmignore` - 렌더링시 제외파일 지정: 배포 안할 yaml 파일, 렌더링 시 에러를 유발하는 파일들을 명시
    - `Chart.yaml` - 차트 기본정보 선언
    - `values.yaml` - 배포될 yaml 파일에 들어가 있는 변수들의 기본 값 선언

## 3) 탬플릿 구성파일의 값 바인딩

<img src="https://github.com/user-attachments/assets/f1e5707e-6331-481b-a960-f7a88fc37f55" alt="adder" width="100%" />

### (1) _helpers.tpl

- **정의**
    - 사용자 정의 전역변수를 선언하는 파일
- **범위**
    - 같은 depth의 `templates` 디렉토리에 속한 파일들에서 모두 바인딩하여 사용이 가능

      <img src="https://github.com/user-attachments/assets/285ce7c7-bd60-40ed-aa8d-777b6f296a53" alt="adder" width="50%" />

- **선언**

    ```groovy
    {{- define "api-tester.name" -}}
    {{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
    {{- end }}
    ```

    - `define` 으로 변수명을 선언한 후, 값을 정의한다. 값을 정의하는 곳에서는 `{{- if }}`와 같은 분기처리 등도 가능하다.

        ```groovy
        {{- define "api-tester.fullname" -}}
        {{- if .Values.fullnameOverride }}
        {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
        {{- else }}
        {{- $name := default .Chart.Name .Values.nameOverride }}
        {{- if contains $name .Release.Name }}
        {{- .Release.Name | trunc 63 | trimSuffix "-" }}
        {{- else }}
        {{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
        {{- end }}
        {{- end }}
        {{- end }}
        ```

- **사용**
    - `include` 라는 지시어로 사용 가능

        ```yaml
        apiVersion: apps/v1
        kind: Deployment
        metadata:
          name: {{ include "api-tester.fullname" . }}
        ```


### (2) helm command

- **정의**
    - helm 을 사용하여 쿠버네티스에 배포를 실행하는 명령어
- **범위**
    - `templates` 의 디렉토리에 속한 파일들에서 모두 바인딩하여 사용이 가능
- **선언**
    - `helm install api-tester-2221 ./api-tester -n anotherclass-222`
- **사용**
    - `. Release`로 참조하여 사용

        ```yaml
        {{ .Release.Name }} --> api-tester-2221
        {{ .Release.Namespace }} --> anotherclass-222
        {{ .Release.Service }} --> helm
        ```


### (3) Chart.yaml

- **정의**
    - 헬름 차트(혹은 패키지)의 기본 정보 선언
- **범위**
    - `templates` 의 디렉토리에 속한 파일들에서 모두 바인딩하여 사용이 가능
- **선언**

    ```yaml
    apiVersion: v2
    name: api-tester
    description: A Helm chart for Kubernetes
    type: application
    version: 0.1.0
    appVersion: "v1.0.0"
    ```

- **사용**
    - `. Chart` 로 참조하여 사용 (단, 대문자 조심!!)

        ```yaml
        {{ .Chart.Name }}
        {{ .Chart.Type }}
        {{ .Chart.Version }}
        {{ .Chart.AppVersion }}
        ```


### (4) values.yaml

- **정의**
    - 배포될 yaml 파일에 들어가있는 변수들의 기본값 선언
    - 값은 추가 가능
- **범위**
    - `templates` 의 디렉토리에 속한 파일들에서 모두 바인딩하여 사용이 가능
- **선언**

    ```yaml
    replicaCount: 2
    
    resources:
      requests:
        memory: "100Mi"
        cpu: "100m"
      limits:
        memory: "200Mi"
        cpu: "200m"
    ```

- **사용**
    - `. Values` 로 참조하여 사용

        ```yaml
        spec:
          {{- if not .Values.autoscaling.enabled }}
          replicas: {{ .Values.replicaCount }}
          
          ... (중간 계층 생략) ...
          
                    resources:
                    {{- toYaml .Values.resources | nindent 12 }}
        ```

- **특이사항**
    - `Chart.yaml`에 `appVersion`은 `values.yaml`의 `image.tag` 위치에 Override 된다.

        ```yaml
        # values.yaml
        
        image:
          repository: 1pro/api-tester
          pullPolicy: Always
          # Overrides the image tag whose default is the chart appVersion.
          tag: ""
        ```


### (5) nindent

어떤 값들은 블럭 자체를 끌어와야 하는 경우가 생긴다. 이 때, 계층을 그대로 유지하기 위해 `nindent` 옵션을 사용한다.

- values.yaml

    ```yaml
    configmap:
      data:
        properties:
          spring_profiles_active: "dev"
          application_role: "ALL"
          postgresql_filepath: "/usr/src/myapp/datasource/postgresql-info.yaml"
    ```

- template/configmap.yaml

    ```yaml
    data:
      {{- toYaml .Values.configmap.data.properties | nindent 2 }}
    ```

  configmap.yaml 에서 `data:` 아래에는 properties 내용들이 계층에 맞게 추가되어야 한다.

  그러나 `{{- toYaml ~~~` 이 부분에서 `-`는 왼쪽공백을 모두 제거하라는 뜻이 되는데, `nindent 2` 옵션이 없이 주입하게 되면 아래처럼 주입이 될 것이다.

    ```yaml
    data:
    spring_profiles_active: "dev"
    application_role: "ALL"
    postgresql_filepath: "/usr/src/myapp/datasource/postgresql-info.yaml"
    ```

  계층 구조를 살리기 위해 indent를 2칸 주게 되면

    ```yaml
    data:
      spring_profiles_active: "dev"
      application_role: "ALL"
      postgresql_filepath: "/usr/src/myapp/datasource/postgresql-info.yaml"
    ```

  이렇게 값이 주입되게 될 것이다.


## 4) Jenkins pipeline으로 배포

### Jenkinsfile

```yaml
pipeline {
    agent any

    tools {
        gradle 'gradle-7.6.1'
        jdk 'jdk-17'
    }

    environment {
        // 본인의 username으로 하실 분은 수정해주세요.
        DOCKERHUB_USERNAME = '1pro'
        GITHUB_URL = 'https://github.com/k8s-1pro/kubernetes-anotherclass-sprint2.git'

        // 실습 넘버링
        CLASS_NUM = '2221'
    }

    stages {
        stage('소스파일 체크아웃') {
            steps {
                // 소스코드를 가져올 Github 주소
                git branch: 'main', url: 'https://github.com/k8s-1pro/kubernetes-anotherclass-api-tester.git'
            }
        }

        stage('소스 빌드') {
            steps {
                // 755권한 필요 (윈도우에서 Git으로 소스 업로드시 권한은 644)
                echo "chmod +x ./gradlew"
                echo "gradle clean build"
            }
        }

        stage('릴리즈파일 체크아웃') {
            steps {
                checkout scmGit(branches: [[name: '*/main']],
                    extensions: [[$class: 'SparseCheckoutPaths',
                    sparseCheckoutPaths: [[path: "/${CLASS_NUM}"]]]],
					userRemoteConfigs: [[url: "${GITHUB_URL}"]])
            }
        }

        stage('컨테이너 빌드') {
            steps {
                // jar 파일 복사
                echo "cp ./build/libs/app-0.0.1-SNAPSHOT.jar ./${CLASS_NUM}/build/docker/app-0.0.1-SNAPSHOT.jar"

                // 도커 빌드
                echo "docker build -t ${DOCKERHUB_USERNAME}/api-tester:v1.0.0 ./${CLASS_NUM}/build/docker"
            }
        }

        stage('컨테이너 업로드') {
            steps {
                // DockerHub로 이미지 업로드
                echo "docker push ${DOCKERHUB_USERNAME}/api-tester:v1.0.0"
            }
        }

        stage('헬름 템플릿 확인') {
            steps {
                // K8S 배포
                sh "helm template api-tester-${CLASS_NUM} ./${CLASS_NUM}/deploy/helm/4.addition/api-tester -n anotherclass-222 --create-namespace"
            }
        }

        stage('헬름 배포') {
            steps {
                input message: '배포 시작', ok: "Yes"
                sh "helm upgrade api-tester-${CLASS_NUM} ./${CLASS_NUM}/deploy/helm/4.addition/api-tester -n anotherclass-222 --create-namespace --install"
            }
        }
    }
}
```

### 헬름 템플릿 확인 stage

- Jenkinsfile 배포 pipeline stage 중 중요한 부분은 바로 `헬름 템플릿 확인` 부분이다.
- 실제 배포가 시작되기 전에 `helm template` 명령어를 통해서 배포될 yaml 파일을 확인하는 단계를 넣을 수 있다. 실수를 줄이는 좋은 방법이 될 수 있다.
- `helm template {배포명} {배포할 패키지 경로} -n {namespace명} --create-namespace`
    - `--create-namespace` 는 namespace가 없으면 생성하라는 옵션

### 헬름 배포 stage

- `helm upgrade ~~~~~~ --install` 명령어는 kubectl로 치면 `apply` 명령어라고 보면 된다.

    | **명령어** | **helm** | **kubectl** |
    | --- | --- | --- |
    | 설치 (최초 배포에만) | install | create |
    | 수정 (이후 배포에는) | upgrade | patch |
    | 설치 혹은 수정 (최초 & 이후 모두) | upgrade ~~~ —install | apply |