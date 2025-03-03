# 미니PC에 우분투 설치

---

윈도우 서버에 WSL로 우분투를 설치했다.
그러나,,, 실제로 사용하려다보니 mini pc의 윈도우 <-> WSL 바인딩 및 작업에서 불편한 점이 매우 많았다.
개발이 이루어지는 Desktop에서는 WSL이 그렇게 불편하지 않았는데, CI/CD가 되는 서버에서는 역시 Native Linux 서버가 있는게 편했다...

## 윈도우 파티션

- [누나IT 영상: 윈도우 파티션 방법](https://youtu.be/1UBoiimmzF4?si=98USJVVbomcBRcMZ)

나는 윈도우는 굳이 필요가 없어서 (혹시 모르니 OS는 살려두기로 하고) 512GB 중 350GB 정도를 리눅스 서버에 할당했다.

## 미니 PC 우분투 설치

- [정리가 잘 된 Lukaid님의 블로그](https://velog.io/@lukaid/N100-mini-pc%EB%A1%9C-Ubuntu-Server-%EB%A7%8C%EB%93%A4%EA%B8%B0-1)

시리즈로 정리가 잘 되어있다. 다만 세팅을 할 때 환경별로 조금씩 다른 부분들이 있어서 궁금한 부분들만 추가적으로 검색하며 설치했다.

### 언어 설치

나는 [우분투 24.04.1 LTS 버전을 설치](https://ubuntu.com/download/desktop)했다. 언어 설치가 잘 
언어 설치 부분에서 조금 안되는 상황이 있었는데, 시스템 종료 후 1-2번 재부팅 및 재설치를 하여 성공했다.

### 네트워크 설정 및 ssh 세팅

네트워크는 기존 글에서 이미 세팅을 했었다. ssh 서버도 설치해서 접속이 잘 진행되는것을 확인했다.
> 기존에 윈도우 서버로 접속했던 기록이 있어서 접속 key가 겹쳐 에러가 나는 경우가 있다. 
> 
> `REMOTE HOST IDENTIFICATION HAS CHANGED!!`
> 
> 해당 에러이며, 해결은 ssh keygen을 다시 발급하면 된다. [방법](https://visu4l.tistory.com/entry/ssh-%EC%9B%90%EA%B2%A9-%EC%A0%91%EC%86%8D-%EC%97%90%EB%9F%ACWARNING-REMOTE-HOST-IDENTIFICATION-HAS-CHANGED)

### 부팅 순서 변경

이제 미니 pc를 켜면 우분투로 부팅이 되게 하고 싶어졌다.

- [행복을 드리는 클로버 블로그](https://newsisf.tistory.com/81)

사진이 남아있지는 않지만, 처음에는 부팅 순서를 설정하는데, 도저히 우분투 파티션을 찾을 수 없었다.
윈도우가 설치되어있는 SSD를 Boot Option 1로 두고, 그 아래쪽을 보면 해당 디스크에서 어떤 부트 매니저를 사용할지 선택하는 칸이 추가로 있었다.

쉽게 설명하면, 
내 PC는 1개의 SSD에 파티션 1은 윈도우, 파티션 2는 우분투가 깔려있는 상황이었는데,
인터넷에 많이 보이는 부팅 순서 변경은 'SSD', 'CD', 'USB' 이런 순서를 바꾸는 것이었고, 그 아래쪽에 'SSD'에서 읽히는 부트 매니저 '1. windosw', '2. linux' 이렇게 선택할 수 있는 칸이 하나 더 있었다.

마지막 Exit 탭에는 Boot Override라는 옵션도 있는데,
이거는 동작시켜보니 그냥 해당 부트 매니저로 한번 부트 하는 옵션이더라.

이제, 부팅 시 자동으로 되겠지 싶었는데, GRUB 부팅 메뉴가 보이게 됐다.
이 화면은 Ubuntu 부트 매니저에서도 어떤 옵션으로 부팅을 할 것인지 고르는 것으로 보였다.

해당 화면에서 어떻게 동작할지 설정하는 파일이 따로 있었다.

- [성혁의 개발 블로그](https://seonghyuk.tistory.com/m/145)

해당 메뉴를 vi로 수정한 후 재부팅하였고, 내가 원하는대로 5초 후 바로 ubuntu로 진입하게 되었다.

진입 후 최초 로그인이 진행되어야 하는데, 나는 ssh만 사용되면 됐고, ssh는 GUI 로그인 없이도 사용이 가능했다.

### vscode의 Remote SSH

뜬금없이 제일 고생했다.
기존에 나처럼 해당 ip로 연결한 적이 있으면, 그 정보가 vscode에 남아있게 된다.

config 파일을 켜서 기존 ip 내용을 지워주면 된다. 
여러 문제가 잘 정리되어 있는 [Luv{Flag} 블로그](https://lovflag.tistory.com/17) 글이다.

그러나, 역시 여기에도 없던 접속이 안되는 상황이 계속 발생했다.
vscode 로그를 자세히 보니, 

```shell
[23:48:57.807] Showing password prompt
[23:49:09.841] Got password response
[23:49:09.841] "install" wrote data to terminal:
[23:49:09.885] >
[23:49:10.192] > bash: line 1: powershell: command not found
"install" terminal command done
[23:49:11.448] Install terminal quit with output: bash: line 1: powershell: command not found
[23:49:11.449] Received install output: bash: line 1: powershell: command not found
[23:49:11.449] Failed to parse remote port from server output
[23:49:11.450] Resolver error: Error:
at v.Create (c:\Users\c\.vscode\extensions\ms-vscode-remote.remote-ssh-0.115.0\out\extension. js: 2:493431)
at t.handleInstallOutput (c:\Users\c\. vscode\extensions\ms-vscode-remote.remote-ssh-0.115.0\out\extension. js: 2:490753)
at t.tryInstall (c:\Users\c\.vscode\extensions\ms-vscode-remote.remote-ssh-0.115.0\out\extension.js: 2:608797)
at async c:\Users\c\.vscode\extensions\ms-vscode-remote.remote-ssh-0.115.0\out\extension.js: 2:568008
at async t.withShowDetailsEvent (c:\Users\c\.vscode\extensions\ms-vscode-remote.remote-ssh-0.115.0\out\extension.
js : 2:571256)
at async P (c:\Users\c\. vscode\extensions\ms-vscode-remote.remote-ssh-0.115.0\out\extension. js:2:564794)
at async t.resolve (c:\Users\c\.vscode\extensions\ms-vscode-remote.remote-ssh-0.115.0\out\extension. js: 2:568667)
at async c:\Users\c\.vscode\extensions\ms-vscode-remote.remote-ssh-0.115.0\out\extension.js:2:839059
[23:49:11.453]

[23:49:11.448]
```

5번째 줄에 보이는 bash: line 1: powershell: command not found...?
나는 우분투인데 웬 powershell?

아.. 운영체제가 잘못 잡혀있나보다. 운영체제를 변경해야한다. 잘 검색이 안됐는데, 어째 저째 찾았다.
`C:Users\{유저명}\AppData\Roaming\Code\User\settings.json`
위치가 AppData인 만큼 캐싱으로 다시 지워지는 경우도 있을 것인데, 나는 계속 잡혀있어서 직접 수정해준 것 같다.

---

Native 우분투 서버가 생기니 너무 좋다 :)