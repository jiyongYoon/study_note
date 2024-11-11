# Jenkins Controller & agent 세팅

---

집에 마련한 서버에 CI/CD를 자동화하는데 도움을 주는 jenkins를 세팅해보았다.
세팅하면서 생긴 문제들을 간단하게 정리한다.

### Jenkins 설치 관련 잘 정리된 블로그

https://nirsa.tistory.com/302
https://holjjack.tistory.com/275
https://garve32.tistory.com/45

### Agent 연결

- 설치
  ```shell
  # 호스트 주소는 Controller가 설치된 노드 입력
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ curl -sO http://192.168.0.43:9090/jnlpJars/agent.jar
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ ls
  agent.jar <-- 설치된 agent.jar 파일
  ```

- 실행
  ```shell
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ java -jar agent.jar -url http://192.168.0.43:9090/ -secret f2b9eca9b243c060dde20ec1333e984128b8a0a5a695d75b9b58ce1fb597b35f -name "jyyoon_mini" -workDir ""
  Exception in thread "main" java.nio.file.AccessDeniedException: /remoting
          at java.base/sun.nio.fs.UnixException.translateToIOException(UnixException.java:90)
          at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:106)
          at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:111)
          at java.base/sun.nio.fs.UnixFileSystemProvider.createDirectory(UnixFileSystemProvider.java:438)
          at java.base/java.nio.file.Files.createDirectory(Files.java:699)
          at java.base/java.nio.file.Files.createAndCheckIsDirectory(Files.java:807)
          at java.base/java.nio.file.Files.createDirectories(Files.java:793)
          at org.jenkinsci.remoting.engine.WorkDirManager.initializeWorkDir(WorkDirManager.java:211)
          at hudson.remoting.Launcher.initialize(Launcher.java:465)
          at hudson.remoting.Launcher.run(Launcher.java:444)
          at hudson.remoting.Launcher.main(Launcher.java:416)
  ```
  
  -> 권한 문제: https://yoonwould.tistory.com/162
  
  ```shell
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ java -jar agent.jar -url http://192.168.0.43:9090/ -secret f2b9eca9b243c060dde20ec1333e984128b8a0a5a695d75b9b58ce1fb597b35f -name "jyyoon_mini" -workDir "./"
  Oct 31, 2024 1:43:26 AM org.jenkinsci.remoting.engine.WorkDirManager initializeWorkDir
  INFO: Using ./remoting as a remoting work directory
  Oct 31, 2024 1:43:26 AM org.jenkinsci.remoting.engine.WorkDirManager setupLogging
  INFO: Both error and output logs will be printed to ./remoting
  Oct 31, 2024 1:43:26 AM hudson.remoting.Launcher createEngine
  INFO: Setting up agent: jyyoon_mini
  Oct 31, 2024 1:43:26 AM hudson.remoting.Engine startEngine
  INFO: Using Remoting version: 3248.3250.v3277a_8e88c9b_
  Oct 31, 2024 1:43:26 AM org.jenkinsci.remoting.engine.WorkDirManager initializeWorkDir
  INFO: Using ./remoting as a remoting work directory
  Oct 31, 2024 1:43:26 AM hudson.remoting.Launcher$CuiListener status
  INFO: Locating server among [http://192.168.0.43:9090/]
  Oct 31, 2024 1:43:27 AM org.jenkinsci.remoting.engine.JnlpAgentEndpointResolver resolve
  INFO: Remoting server accepts the following protocols: [JNLP4-connect, Ping]
  Oct 31, 2024 1:43:27 AM hudson.remoting.Launcher$CuiListener status
  INFO: Agent discovery successful
    Agent address: 192.168.0.43
    Agent port:    50000
    Identity:      81:ca:40:df:f3:12:31:cc:c5:63:5e:39:ac:48:bc:4c
  Oct 31, 2024 1:43:27 AM hudson.remoting.Launcher$CuiListener status
  INFO: Handshaking
  Oct 31, 2024 1:43:27 AM hudson.remoting.Launcher$CuiListener status
  INFO: Connecting to 192.168.0.43:50000
  Oct 31, 2024 1:43:27 AM hudson.remoting.Launcher$CuiListener status
  INFO: Server reports protocol JNLP4-connect-proxy not supported, skipping
  Oct 31, 2024 1:43:27 AM hudson.remoting.Launcher$CuiListener status
  INFO: Trying protocol: JNLP4-connect
  Oct 31, 2024 1:43:27 AM org.jenkinsci.remoting.protocol.impl.BIONetworkLayer$Reader run
  INFO: Waiting for ProtocolStack to start.
  Oct 31, 2024 1:43:28 AM hudson.remoting.Launcher$CuiListener status
  INFO: Remote identity confirmed: 81:ca:40:df:f3:12:31:cc:c5:63:5e:39:ac:48:bc:4c
  Oct 31, 2024 1:43:28 AM hudson.remoting.Launcher$CuiListener status
  INFO: Connected
  ```

### 실행

- 백그라운드 실행
  ```shell
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ nohup java -jar agent.jar -url http://192.168.0.43:9090/ -secret f2b9eca9b243c060dde20ec1333e984128b8a0a5a695d75b9b58ce1fb597b35f -name "jyyoon_mini" -workDir "./&
  [1] 2373
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ nohup: ignoring input and appending output to 'nohup.out'
  
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ ls -al
  total 1368
  drwxrwxrwx 1 jyyoon_mini jyyoon_mini     512 Oct 31 01:49 .
  drwxrwxrwx 1 jyyoon_mini jyyoon_mini     512 Oct 31 01:30 ..
  -rwxrwxrwx 1 jyyoon_mini jyyoon_mini 1393029 Oct 31 01:37 agent.jar
  -rwxrwxrwx 1 jyyoon_mini jyyoon_mini    3894 Oct 31 01:50 nohup.out
  drwxrwxrwx 1 jyyoon_mini jyyoon_mini     512 Oct 31 01:43 remoting
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ cat nohup.out
  Oct 31, 2024 1:49:52 AM org.jenkinsci.remoting.engine.WorkDirManager initializeWorkDir
  INFO: Using ./remoting as a remoting work directory
  Oct 31, 2024 1:49:52 AM org.jenkinsci.remoting.engine.WorkDirManager setupLogging
  INFO: Both error and output logs will be printed to ./remoting
  Oct 31, 2024 1:49:52 AM hudson.remoting.Launcher createEngine
  INFO: Setting up agent: jyyoon_mini
  Oct 31, 2024 1:49:52 AM hudson.remoting.Engine startEngine
  INFO: Using Remoting version: 3248.3250.v3277a_8e88c9b_
  Oct 31, 2024 1:49:52 AM org.jenkinsci.remoting.engine.WorkDirManager initializeWorkDir
  INFO: Using ./remoting as a remoting work directory
  Oct 31, 2024 1:49:52 AM hudson.remoting.Launcher$CuiListener status
  INFO: Locating server among [http://192.168.0.43:9090/]
  Oct 31, 2024 1:49:52 AM org.jenkinsci.remoting.engine.JnlpAgentEndpointResolver resolve
  INFO: Remoting server accepts the following protocols: [JNLP4-connect, Ping]
  Oct 31, 2024 1:49:52 AM hudson.remoting.Launcher$CuiListener status
  INFO: Agent discovery successful
    Agent address: 192.168.0.43
    Agent port:    50000
    Identity:      81:ca:40:df:f3:12:31:cc:c5:63:5e:39:ac:48:bc:4c
  Oct 31, 2024 1:49:52 AM hudson.remoting.Launcher$CuiListener status
  INFO: Handshaking
  Oct 31, 2024 1:49:52 AM hudson.remoting.Launcher$CuiListener status
  INFO: Connecting to 192.168.0.43:50000
  Oct 31, 2024 1:49:52 AM hudson.remoting.Launcher$CuiListener status
  INFO: Server reports protocol JNLP4-connect-proxy not supported, skipping
  Oct 31, 2024 1:49:52 AM hudson.remoting.Launcher$CuiListener status
  INFO: Trying protocol: JNLP4-connect
  Oct 31, 2024 1:49:53 AM org.jenkinsci.remoting.protocol.impl.BIONetworkLayer$Reader run
  INFO: Waiting for ProtocolStack to start.
  Oct 31, 2024 1:49:54 AM hudson.remoting.Launcher$CuiListener status
  INFO: Remote identity confirmed: 81:ca:40:df:f3:12:31:cc:c5:63:5e:39:ac:48:bc:4c
  Oct 31, 2024 1:49:54 AM hudson.remoting.Launcher$CuiListener status
  INFO: Connected
  Oct 31, 2024 1:50:21 AM org.jenkinsci.remoting.engine.WorkDirManager initializeWorkDir
  INFO: Using ./remoting as a remoting work directory
  Oct 31, 2024 1:50:21 AM org.jenkinsci.remoting.engine.WorkDirManager setupLogging
  INFO: Both error and output logs will be printed to ./remoting
  Oct 31, 2024 1:50:21 AM hudson.remoting.Launcher createEngine
  INFO: Setting up agent: jyyoon_mini
  Oct 31, 2024 1:50:21 AM hudson.remoting.Engine startEngine
  INFO: Using Remoting version: 3248.3250.v3277a_8e88c9b_
  Oct 31, 2024 1:50:21 AM org.jenkinsci.remoting.engine.WorkDirManager initializeWorkDir
  INFO: Using ./remoting as a remoting work directory
  Oct 31, 2024 1:50:21 AM hudson.remoting.Launcher$CuiListener status
  INFO: Locating server among [http://192.168.0.43:9090/]
  Oct 31, 2024 1:50:22 AM org.jenkinsci.remoting.engine.JnlpAgentEndpointResolver resolve
  INFO: Remoting server accepts the following protocols: [JNLP4-connect, Ping]
  Oct 31, 2024 1:50:22 AM hudson.remoting.Launcher$CuiListener status
  INFO: Agent discovery successful
    Agent address: 192.168.0.43
    Agent port:    50000
    Identity:      81:ca:40:df:f3:12:31:cc:c5:63:5e:39:ac:48:bc:4c
  Oct 31, 2024 1:50:22 AM hudson.remoting.Launcher$CuiListener status
  INFO: Handshaking
  Oct 31, 2024 1:50:22 AM hudson.remoting.Launcher$CuiListener status
  INFO: Connecting to 192.168.0.43:50000
  Oct 31, 2024 1:50:22 AM hudson.remoting.Launcher$CuiListener status
  INFO: Server reports protocol JNLP4-connect-proxy not supported, skipping
  Oct 31, 2024 1:50:22 AM hudson.remoting.Launcher$CuiListener status
  INFO: Trying protocol: JNLP4-connect
  Oct 31, 2024 1:50:22 AM org.jenkinsci.remoting.protocol.impl.BIONetworkLayer$Reader run
  INFO: Waiting for ProtocolStack to start.
  Oct 31, 2024 1:50:23 AM hudson.remoting.Launcher$CuiListener status
  INFO: Remote identity confirmed: 81:ca:40:df:f3:12:31:cc:c5:63:5e:39:ac:48:bc:4c
  Oct 31, 2024 1:50:23 AM hudson.remoting.Launcher$CuiListener status
  INFO: Connected
  ```

### 운영

- 프로세스 찾기
  ```shell
   ps -ef | grep 'agent.jar'
  ```
  
  ```shell
   jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ ps -ef | grep 'agent.jar'
   UID          PID    PPID  C STIME TTY          TIME CMD
   jyyoon_+    2373    1613  2 01:49 pts/0    00:00:10 java -jar agent.jar -url http://192.168.0.43:9090/ -secret f2b9eca9b243c060dde20ec1333e984128b8a0a5a695d75b9b58ce1fb597b35f -name jyyoon_mini -workDir ./
   jyyoon_+    2448    1613  0 01:56 pts/0    00:00:00 grep --color=auto agent.jar
  ```

### 실행 테스트

- agent test: https://youtu.be/V2ejGOY_uJI?si=80BFY62SqIXQuLuc&t=604

- output
  ```text
  Started by user admin
  Running as SYSTEM
  Building remotely on jyyoon_mini in workspace /mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins/workspace/agent-test
  [agent-test] $ /bin/sh -xe /tmp/jenkins13251017801095968873.sh
  + echo Hello Agent!!
  Finished: SUCCESS
  ```
  
  ```shell
  jyyoon_mini@DESKTOP-BL2BK6J:/mnt/c/Users/jyyoon_mini/Desktop/dev/jenkins$ cat workspace/agent-test/hello.txt
  Hello Agent!!
  ```