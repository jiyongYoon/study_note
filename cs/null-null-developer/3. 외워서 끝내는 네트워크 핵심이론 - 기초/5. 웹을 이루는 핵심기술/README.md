# 웹을 이루는 핵심기술

---

### DNS

- ip 주소를 알려주는 인터넷 상의 서버
- 우리가 주소창에 입력하는 주소는 보통 사람이 알아보기 쉬운 영문 주소다.

    <img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/bb86c998-7ef0-454f-b8b8-d1900eaadc50" alt="adder" width="60%" />

    - 미국식 주소체계처럼 뒤에서부터 큰 범위다.
    - 즉, `com` 안에 `naver` 안에 `www`다.

### DNS 서버 동작 순서

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/03fe7a2e-bc6f-4097-9210-b577d2474761" alt="adder" width="80%" />

- 보통 ISP가 가진 DNS 서버가 있다.

1. DNS 서버에 접속하고자 하는 주소의 실제 ip 주소를 묻는다.
2. DNS 서버가 가진 ip 주소를 응답받는다.
  - 응답 시 유효기간까지 함께 전달한다.
3. 응답받은 ip주소를 로컬 DNS Cache에 저장하고, 실제 ip를 찾아간다.
4. 이후 유호기간이 지나기 전까지는 DNS Cache에 저장된 ip주소를 바로 사용한다.

**만약 DNS 서버가 ip를 모르면??**

<img src="https://github.com/jiyongYoon/study_cs_note/assets/98104603/9f8a10b0-dfc9-4671-a5c0-b3b15f902189" alt="adder" width="80%" />

1. ISP의 DNS에게 접속할 주소의 ip를 묻는다.
2. 나 모르는데?
3. Root DNS에게 도메인 범위부터의 DNS 서버목록을 요청한다.
4. `.com`에 대해 관리하는 DNS 서버 ip 목록을 준다.
5. 해당 DNS 서버들 중 `naver`에 대해 관리하고 있는 DNS 서버를 찾는다. (naver 사업자의 DNS일 것이다.)
6. 그에 해당하는 DNS 서버 ip 주소를 받아온다.
7. DNS 서버에 `www` 에 해당하는 서버의 ip를 요청한다.
8. 응답받는다.
9. 받은 응답을 본인(ISP의 DNS)이 저장한다.
10. 최초 요청했던 클라이언트에게 ip주소를 응답한다.

- 클라이언트는 두가지 목록을 가지고 있다.
  - DNS Cahce
    - DNS에게 한번 물어본 내용을 유효기간동안 캐싱하여 사용한다.
  - 호스트파일
    - DNS에 의존하지 않기 위해 기술하는 파일이며, 이 목록에 있는 경우 DNS에 물어보지 않고 바로 사용한다.
    - 윈도우는 보통 `C:\Windows\System32\drivers\etc`에 `hosts`라는 이름으로 위치해있다.
    - 열어보니까, 기본적인 ip주소들이 있고, Docker Desktop에서 추가한 내용(내부적으로 매핑해서 사용하는 용도의 properties)이 적혀있네!
      ```
      # Copyright (c) 1993-2009 Microsoft Corp.
      #
      # This is a sample HOSTS file used by Microsoft TCP/IP for Windows.
      #
      # This file contains the mappings of IP addresses to host names. Each
      # entry should be kept on an individual line. The IP address should
      # be placed in the first column followed by the corresponding host name.
      # The IP address and the host name should be separated by at least one
      # space.
      #
      # Additionally, comments (such as these) may be inserted on individual
      # lines or following the machine name denoted by a '#' symbol.
      #
      # For example:
      #
      #      102.54.94.97     rhino.acme.com          # source server
      #       38.25.63.10     x.acme.com              # x client host
      
      # localhost name resolution is handled within DNS itself.
      #	127.0.0.1       localhost
      #	::1             localhost
      127.0.0.1                   activate.adobe.com
      127.0.0.1                   practivate.adobe.com
      127.0.0.1                   lmlicenses.wip4.adobe.com
      127.0.0.1                   lm.licenses.adobe.com
      # Added by Docker Desktop
      192.168.0.2 host.docker.internal
      192.168.0.2 gateway.docker.internal
      # To allow the same kube context to work on the host and the container:
      127.0.0.1 kubernetes.docker.internal
      # End of section

      ```
      - 만약 이 부분을 수정하게 되면, DNS에 물어보지 않고 바로 접속을 다른 곳으로 유도할 수 있을 것이다.

> 즉, 실제 ip주소를 입력하여 접속하지 않는 한, DNS 서버가 알려준 곳으로 (또는 호스트 파일이 작성되어 있는 주소로) 가게 될 것이다. <br>
> 보안이 엄청나게 중요해지는 이유이다!!

### 웹 기술의 창시

- 팀 버너스리가 `정보검색 시스템 구축`을 한 것이 기반이 되었다.
  - `HTML`이라는 `문서`를 실어나르기 위한 `규약`인 `HTTP` 를 만듦.

### URL과 URI

- URL: Uniform Resource Locator
- URI: Uniform Resource Identifier

- URL 형식
  ```
  Protocol://Address:Port-number/Path(or filename)?Parameter=value
  ```
  - Port 번호는 아무것도 안쓰면 기본적으로 TCP(80) 포트로 요청하게 된다.
  - Path는 Web-Server 내부의 file의 절대경로를 전부 작성하는 것이 아니다.
  - Web-Server가 기준점으로 잡고 있는 Root 위치에서부터의 상대경로를 작성하게 된다!
  - 아무것도 작성하지 않는 경우에는 `index.html`이 생략된 것이다.

### HTTP

- HTTP는 HTML 문서를 전송받기 위해 만들어진 `응용 프로그램 계층 통신 프로토콜(L7 Layer)`이다.
  - L5 이상에서는 Socket 통신이다.
  - 단위는 `Stream`이며, 시작만 있고 끝은 해석을 해야하기 때문에 HTTP 내부에 정보가 있게 된다.
- HTTP는 문자열로 구성되어 있다.
- 헤더는 크게 `요청`, `응답`으로 구분된다.
- 요청에 사용되는 메서드는 주로 `GET`, `POST` 이다.