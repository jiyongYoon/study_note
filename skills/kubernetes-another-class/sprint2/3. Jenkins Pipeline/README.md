# 3. Jenkins Pipeline

---

# 1. 배포 전략을 위한 제언

![SmartSelect_20241005_222024_Samsung Notes.jpg](https://github.com/user-attachments/assets/021e07d9-4b53-4271-80ba-9f6d58bc85f6)

- 정답이 있는 것이 아니기 때문에 사용하는 배포툴 및 특징을 생각하여 배포 전략을 세워야 함.

# 2. Jenkins Pipeline 기본 구성

### Pipeline 아이템 생성

- 사용하는 Script

    ```groovy
    pipeline {
        agent any
    
        tools {
            gradle 'gradle-7.6.1'
            jdk 'jdk-17'
        }
    
        environment {
            DOCKERHUB_USERNAME = 'jyyoon0615'
            GITHUB_URL = 'https://github.com/jiyongYoon/kubernetes-anotherclass-sprint2.git'
    
            // 실습 넘버링 - (수정x)
            CLASS_NUM = '2211'
        }
    
        stages {
            stage('Source Build') {
                steps {
                    // 소스파일 체크아웃
                    git branch: 'main', url: 'https://github.com/k8s-1pro/kubernetes-anotherclass-api-tester.git'
    
                    // 소스 빌드
                    // 755권한 필요 (윈도우에서 Git으로 소스 업로드시 권한은 644)
                    sh "chmod +x ./gradlew"
                    sh "gradle clean build"
                }
            }
    
            stage('Container Build') {
                steps {	
                    // 릴리즈파일 체크아웃
                    checkout scmGit(branches: [[name: '*/main']], 
                        extensions: [[$class: 'SparseCheckoutPaths', 
                        sparseCheckoutPaths: [[path: "/${CLASS_NUM}"]]]], 
                        userRemoteConfigs: [[url: "${GITHUB_URL}"]])
    
                    // jar 파일 복사
                    sh "cp ./build/libs/app-0.0.1-SNAPSHOT.jar ./${CLASS_NUM}/build/docker/app-0.0.1-SNAPSHOT.jar"
    
                    // 컨테이너 빌드 및 업로드
                    sh "docker build -t ${DOCKERHUB_USERNAME}/api-tester:v1.0.0 ./${CLASS_NUM}/build/docker"
                    script{
                        if (DOCKERHUB_USERNAME == "1pro") {
                            echo "docker push ${DOCKERHUB_USERNAME}/api-tester:v1.0.0"
                        } else {
                            sh "docker push ${DOCKERHUB_USERNAME}/api-tester:v1.0.0"
                        }
                    }
                }
            }
    
            stage('K8S Deploy') {
                steps {
                    // 쿠버네티스 배포 
                    sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/namespace.yaml"
    				sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/configmap.yaml"
    				sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/secret.yaml"
    				sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/service.yaml"
    				sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/deployment.yaml"
                }
            }
        }
    }
    ```

    - **Agent**
        - **agent any :** 사용 가능한 에이전트에서 파이프라인 Stage를 실행, Master나 Salve 아무곳에서나 Stage가 실행됨
        - **agent label(node) :** 지정된 레이블(노드)에서 Stage가 실행
        - **agent docker :** Docker 빌드를 제공해주는 Agent 사용
        - **agent dockerfile :** Dockerfile 을 직접 쓸 수 있는 Agent 사용
    - **Jenkins Docs : https://www.jenkins.io/doc/book/pipeline/syntax/**

### 빌드 결과

![image.png](https://github.com/user-attachments/assets/183425a4-3d75-4cb2-8885-7d051863c844)

![image](https://github.com/user-attachments/assets/58295f11-9ddb-4593-ab95-f69c1adb7295)

- 아래와 같은 jenkins 스크립트는 빌드를 위해 여러 스크립트를 한 번에 돌려야 할 때, Jenkins를 master-slave로 나누어 부하를 분산하여 돌리는 경우에 많이 사용한다.

# 3. Github 연결 및 파이프라인 세분화

### Pipeline 아이템 생성

- `Configure` - `General` - `GitHub project` 에 CI/CD 관련 설정파일들이 있는 github repo 세팅
- `Configure` - `Advanced Project Options` - `Pipeline` 에서
    - Pipeline script from SCM(Softwafe Configuration Management, 소프트웨어 형상관리, 즉 github을 이야기함)  을 선택하고  CI/CD 관련 설정파일들이 있는 github repo 세팅
    - Script Path 에 `JenkinsFile`이 있는 위치를 입력하면 해당 JenkinsFile을 사용하여 빌드를 실행함

### 빌드 결과

![image](https://github.com/user-attachments/assets/d390824a-7bb4-49cd-b049-a3abbda654fb)

- 사용한 Jenkins 스크립트 파일

    ```groovy
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
            CLASS_NUM = '2212'
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
                    sh "chmod +x ./gradlew"
                    sh "gradle clean build"
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
                    sh "cp ./build/libs/app-0.0.1-SNAPSHOT.jar ./${CLASS_NUM}/build/docker/app-0.0.1-SNAPSHOT.jar"
    
                    // 도커 빌드
                    sh "docker build -t ${DOCKERHUB_USERNAME}/api-tester:v1.0.0 ./${CLASS_NUM}/build/docker"
                }
            }
    
            stage('컨테이너 업로드') {
                steps {
                    // DockerHub로 이미지 업로드
                    script{
                        if (DOCKERHUB_USERNAME == "1pro") {
                            echo "docker push ${DOCKERHUB_USERNAME}/api-tester:v1.0.0"
                        } else {
                            sh "docker push ${DOCKERHUB_USERNAME}/api-tester:v1.0.0"
                        }
                    }
                }
            }
    
            stage('쿠버네티스 배포') {
                steps {
                    // K8S 배포
                    sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/namespace.yaml"
                    sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/configmap.yaml"
                    sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/secret.yaml"
                    sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/service.yaml"
                    sh "kubectl apply -f ./${CLASS_NUM}/deploy/k8s/deployment.yaml"
                }
            }
    
            stage('배포 확인') {
                steps {
                    // 10초 대기
                    sh "sleep 10"
    
                    // K8S 배포
                    sh "kubectl get -f ./${CLASS_NUM}/deploy/k8s/namespace.yaml"
                    sh "kubectl get -f ./${CLASS_NUM}/deploy/k8s/configmap.yaml"
                    sh "kubectl get -f ./${CLASS_NUM}/deploy/k8s/secret.yaml"
                    sh "kubectl get -f ./${CLASS_NUM}/deploy/k8s/service.yaml"
                    sh "kubectl get -f ./${CLASS_NUM}/deploy/k8s/deployment.yaml"
                }
            }
        }
    }
    ```


# 4. Blue-Green 배포

![image](https://github.com/user-attachments/assets/ccfd285f-fe78-4bbf-969c-3ed194b91f4e)

<img src="https://github.com/user-attachments/assets/1537501e-568b-468e-abab-e13b9215393e" alt="adder" width="40%" />

## 변경할 k8s의 resource

### 1) deployment.yaml

- blue

    ```yaml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      namespace: anotherclass-221
      name: api-tester-2214-1
      labels:
        part-of: k8s-anotherclass
        component: backend-server
        name: api-tester
        instance: api-tester-2214
        version: 1.0.0
        managed-by: kubectl
    spec:
      selector:
        matchLabels:
          part-of: k8s-anotherclass
          component: backend-server
          name: api-tester
          instance: api-tester-2214
      replicas: 2
      strategy:
        type: RollingUpdate
      template:
        metadata:
          labels:
            part-of: k8s-anotherclass
            component: backend-server
            name: api-tester
            instance: api-tester-2214
            version: 1.0.0
            blue-green-no: "1"
        spec:
          nodeSelector:
            kubernetes.io/hostname: k8s-master
          containers:
            - name: api-tester-2214
              image: 1pro/api-tester:v1.0.0
              imagePullPolicy: Always
              ports:
                - containerPort: 8080
                  name: http
              envFrom:
                - configMapRef:
                    name: api-tester-2214-properties
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
                secretName: api-tester-2214-postgresql
    ```


- green

    ```yaml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      namespace: anotherclass-221
      name: api-tester-2214-2          <---------
      labels:
        part-of: k8s-anotherclass
        component: backend-server
        name: api-tester
        instance: api-tester-2214
        version: 2.0.0                  <---------
        managed-by: kubectl
    spec:
      selector:
        matchLabels:
          part-of: k8s-anotherclass
          component: backend-server
          name: api-tester
          instance: api-tester-2214
      replicas: 2
      strategy:
        type: RollingUpdate
      template:
        metadata:
          labels:
            part-of: k8s-anotherclass
            component: backend-server
            name: api-tester
            instance: api-tester-2214
            version: 2.0.0              <---------
            blue-green-no: "2"          <---------
        spec:
          nodeSelector:
            kubernetes.io/hostname: k8s-master
          containers:
            - name: api-tester-2214-2        <---------
              image: 1pro/api-tester:v2.0.0  <---------
              imagePullPolicy: Always
              ports:
                - containerPort: 8080
                  name: http
              envFrom:
                - configMapRef:
                    name: api-tester-2214-properties
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
                secretName: api-tester-2214-postgresql
    ```

- 변경 데이터 1 - `deployment.metadata`
    - deployment를 구분하기 위한 이름, 버전 등의 속성
- 변경 데이터 2 - `template.metadata.labels`
    - Pod를 구분하기 위한 label 정보 → service의 selector가 사용할 정보
- 변경 데이터 3 - `template.metadata.containers`
    - 새로 배포할 소스 코드의 컨테이너 이미지 및 이름

### 2) service.yaml

- jenkinsfile 에서 service.yaml에 내용 수정

    ```groovy
    pipeline {
        agent any
        
        environment {
    		... 생략 ...
        }
    
        stages {
            
    		... 생략 ...
    				
            stage('Green 전환 완료') {
                steps {
                    sh "kubectl patch -n anotherclass-221 svc api-tester-2214 -p '{\"spec\": {\"selector\": {\"blue-green-no\": \"2\"}}}'"
                }
            }
            
            ... 생략 ...
        }
    }
    ```

    - `service.yaml` 의 `spec.selector`의 정보를 1 (blue)→ 2(green) 로 변경하여 트래픽 전환

## green 배포 후 문제가 없는 경우 blue 삭제 및 나머지 정보 수정

- jenkinsfile에서 내용 수정

    ```groovy
    pipeline {
        agent any
        
        environment {
    		... 생략 ...
        }
    
        stages {
            
    		... 생략 ...
    				
            stage('Blue 삭제') {
                steps {
                    sh "kubectl delete -f ./${CLASS_NUM}/deploy/k8s/blue/deployment.yaml"
                    sh "kubectl patch -n anotherclass-221 svc api-tester-2214 -p '{\"metadata\": {\"labels\": {\"version\": \"2.0.0\"}}}'"
                    sh "kubectl patch -n anotherclass-221 cm api-tester-2214-properties -p '{\"metadata\": {\"labels\": {\"version\": \"2.0.0\"}}}'"
                    sh "kubectl patch -n anotherclass-221 secret api-tester-2214-postgresql -p '{\"metadata\": {\"labels\": {\"version\": \"2.0.0\"}}}'"
                }
            }
        }
    }
    ```

    - `service.yaml`, `configmap.yaml` ,`secret.yaml` 의 메타 정보를 green에 해당하는 정보로 변경 (실제 운영 환경 및 트래픽 등에 영향은 없는 정보기는 함)

> 이런 식으로 blue - green 배포가 진행됨을 확인하자.
그러나 실제 실무에서는 이런 식으로 복잡하게 진행하기에는 복잡한 측면이 있다.
> 
> **`ArgoCD`를 활용하면 더욱 간단하게 Blue - Green 배포를 진행할 수 있다!**