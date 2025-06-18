# 5. 응용 계층

---

## 1) DNS(Domain Name Server)

- 사람이 기억하기 쉬운 도메인 이름과 호스트(IP)를 특정지을 주소를 매핑하는 정보를 가진 서버
- IP 주소
  - 기억하기 어려우며
  - 변경될 수 있음

### host 파일
- 개인이 가진 매핑 테이블
- OS마다 위치가 다름
  - Unix, Unix-like, Mac OS: `/etc/hosts`
  - Windows: `%SystemRoot%\System32\drivers\etc\hosts`
    ![img.png](img.png)
    - 내 컴퓨터의 `hosts` 파일인데, Docker Desktop을 설치했더니 `192.168.0.43`과 `host.docker.internal`이 매핑되었다. 즉, 내 호스트는 `host.docker.internal`로 찾아가는게 `192.168.0.43`으로 찾아가는 것과 동일하다는 것이다.

### 계층적 도메인 구조

![img_1.png](img_1.png)

- 루트 도메인은 아무것도 없는 `/`, 전 세계에 몇 대 없음
- 최상위 도메인은 `TLD, Top-Level-Domain`이라고 부름
  - 일반 도메인은 `.com`, `.org`, `.net`, `.edu`, `.gov` 등이 있음
  - 국가별로 국가 도메인이 있으며, 한국은 `kr`

### 서브 도메인 (하위 도메인)

- `wikipedia.com` -> `ko.wikipedia.com` 에서 `ko`가 서브 도메인

### Authoritative DNS 서버

- 찾고자 하는 도메인의 IP 주소를 저장하는 최종 서버. 즉, 내가 원하는 IP 주소가 튀어나오는 서버

### local DNS 서버(DNS Resolver)

- 클라이언트가 가장 먼저 찾는 DNS 서버
- 해당 서버가 계층적 도메인 서버에 질의해서 호스트에 알려주는 역할을 함
  - 반복적 질의, 재귀적 질의 두 방법 중 한 방법으로 질의하나, 결론적으로 local DNS 서버가 IP를 주는 것은 동일함
- 명시적으로 설정도 가능하며(Public DNS, e.g. 8.8.8.8 -> google DNS server)
- 자동 설정도 가능(보통 ISP에서 제공함)

### DNS 레코드

- DNS 서버가 저장하고 있는 데이터
- A 레코드: 도메인에 대한 IPv4 주소 (도메인과 ip주소를 일대일로 매칭)
- AAAA 레코드: 도메인에 대한 IPv6 주소
- CNAME 레코드: 도메인에 대한 별칭
- NS 레코드: 네임 서버 주소
- SOA 레코드: 도메인에 대한 관리자 정보

![img_2.png](img_2.png)
- 위 사진은 내가 구매하여 사용중인 도메인이며, 가비아에서 DNS 관리를 해줌
- 타입은 A, CNAME 등등을 지정할 수 있으며, `호스트.xxxg.site`라는 곳으로 접속을 하면 `값/위치`에 해당하는 ip를 네임서버(ns.gabia.co.kr)이 전달해주는 것이라고 이해하면 됨
- TTL은 캐싱값

## 2) 자원과 자원의 식별

- 자원: 네트워크로 주고받는 정보(파일, 이미지, HTML, XML, JSON...)

### URI(Uniform Resource Identifier)

- 자원 식별자
- URL: 위치 기반 식별(Locator)
- URN: 이름 기반 식별(Name)

![img_3.png](img_3.png)

- scheme: 프로토콜 이름 명시
- authority: [userinfo"@"]host[":"port]
  - 사용자 이름은 생략 가능
  - 포트 번호는 생략하면 기본 포트로 (web -> 80, ftp -> 22 등)
- path: 자원 경로
- query: 파라미터
- fragment: HTML의 북마크 기능, 서버에 전달되지는 않음

## 3) 웹 서버와 웹 애플리케이션 서버

- 웹 서버는 `정적(언제, 어디서, 누가 봐도 변하지 않는)`인 자원을 서빙하고
- 웹 애플리케이션 서버는 `동적(언제, 어디서, 누가 보느냐에 따라 변하는)`인 자원을 서빙함

### 현대 웹 서비스 구성

![img_4.png](img_4.png)

- 역할을 나누어 과도한 부하가 있는 부분을 scale-out 하기 편하도록 구성
- 웹 서버(그림에서는 더 앞단의 Load Balancer) 등의 프록시를 두어 WAS의 노출을 없애 보안 상 이점을 가지고 옴