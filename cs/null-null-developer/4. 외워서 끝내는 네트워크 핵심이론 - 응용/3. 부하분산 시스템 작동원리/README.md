# 부하분산 시스템의 작동원리

## Load Balance

<img src="https://github.com/user-attachments/assets/1efe83f0-dd2c-4f19-979d-147c29ec7cb3" alt="adder" width="100%" />

- Outbound에서 오는 요청에 대해서 `포트 포워딩`으로 요청을 처리할 사설 Host 로 분배한다.
- 분배를 하는 알고리즘에는 여러가지 방법이 있으나, 핵심은 `요청을 처리할 수 있는 서버`를 식별하는 것이다.
  - 그리고 이를 위해 서버의 상태를 확인하는 `Manager` 서버가 필요하며, cpu, memory, disk 등의 metric을 모니터링하여 서버의 상태를 체크한다.
  - 실제 요청이 들어오면 LB(Load Balancer)는 `Manager`에게 어디로 보내면 좋을지 확인받고(`Health Check`), 가장 부하가 적은 서버로 보내게 된다.
- 무중단 시스템
  - 사설망의 서버들은 Auto Scaling으로 관리될 수 있다.
  - 가장 앞단의 LB는 이중화를 통해 관리될 수 있다.

## GSLB (Global Server Load Balancing)

<img src="https://github.com/user-attachments/assets/950b874d-603a-4bcc-819d-b766c0ef49fb" alt="adder" width="100%" />

- 전세계에 서비스하는 서비스들은 앞단의 LB만으로 모든 트래픽을 감당할 수 없다. 
따라서 `DNS 체계를 활용하여 ISP(Internet Service Provider) 내에서 트래픽을 처리`하도록 유도한다.
- GSLB는 기존 DNS에서의 처리 방식(등록된 ip 목록 중 하나를 반환하는 정도)에서 그치지 않고 네트워크 상태, 성능, 트래픽 유입 지점 등을 다양하게 고려하여 처리를 하게 된다.
- DNS 체계를 활용한 GSLB의 트래픽의 개괄적인 처리 흐름
  1. Client가 ISP 내 DNS 서버에 도메인에 해당하는 ip 주소를 질의한다.
  2. DNS는 ip 대신 미리 등록된 alias와 같이 생긴 새로운 domain(`a.www.abc.com`)을 리턴한다.
  3. Clinet는 새로운 domain의 ip를 다시 묻는다.
  4. DNS는 새로운 domain의 ip를 알려준다. 이 ip는 ISP 내에 있는 웹서버로 요청을 보내서 처리한다.
  - 이런 식으로 GSLB의 나름의 정책으로 Client를 어느 서버에 보낼지를 결정하게 된다.
- 그렇다면 문제는 각 ISP에 배포되어있는 서버들의 컨텐츠를 관리해야 한다는 점인데, 이는 `CDN(Content Delivery Network)`을 활용해 컨텐츠를 동기화하게 된다.
  - CDN에 대해서는 나중에 더 자세하게 공부해보자.