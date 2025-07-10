# iptables를 활용한 포트포워딩

- 외부 ip로 접근이 가능한 linux 서버에서 해당 서버가 속한 사설 네트워크로 포트포워딩을 하여 사설 네트워크 서버의 특정 앱에 접근할 수 있도록 해야 했다.

## 1. 포트포워딩 활성화

```shell
# IP 포워딩 활성화 (커널 설정)
sudo sysctl -w net.ipv4.ip_forward=1
echo "net.ipv4.ip_forward = 1" | sudo tee /etc/sysctl.d/99-sysctl.conf
```

- `sudo sysctl -w net.ipv4.ip_forward=1`를 하면 재부팅을 하지 않아도 활성화 된다.
- `/etc/sysctl.d/99-sysctl.conf`에 직접 가서 수정해도 된다.
- iptables가 없으면 설치
  `sudo apt install iptables`

## 2. 포트포워딩 규칙 설정

```shell
sudo iptables -t nat -A PREROUTING -d [DMZ_SERVER_IP] -p tcp --dport [EXTERNAL_PORT] -j DNAT --to-destination [OTHER_SERVER_IP]:[INTERNAL_PORT]
sudo iptables -t nat -A POSTROUTING -p tcp -d [OTHER_SERVER_IP] --dport [INTERNAL_PORT] -j MASQUERADE
```

만약 `192.168.0.100:80` -> `192.168.0.101:8080`로 하고 싶으면

```shell
sudo iptables -t nat -A PREROUTING -d 192.168.0.100 -p tcp --dport 80 -j DNAT --to-destination 192.168.0.101:8080
sudo iptables -t nat -A POSTROUTING -p tcp -d 192.168.0.101 --dport 8080 -j MASQUERADE
```

> PREROUTING 과 POSTROUTING 은 뭐지?
> 
> iptables에서 PREROUTING과 POSTROUTING은 nat 테이블에서 주로 사용되는 체인(chain)이며, 패킷이 리눅스 커널을 통과하는 과정 중 어떤 시점에 주소 변환(NAT)을 수행할 것인지를 결정합니다. 이 두 체인의 핵심적인 차이는 다음과 같습니다.
> 
> 1. PREROUTING 체인
> 시점: 패킷이 리눅스 서버의 네트워크 인터페이스를 통해 들어오자마자, 라우팅 결정이 이루어지기 전에 적용됩니다.
> 주요 용도: Destination NAT (DNAT). 즉, 패킷의 목적지 IP 주소와 포트를 변경할 때 사용합니다.
> 예시: 외부에서 DMZ 서버의 공인 IP (예: 203.0.113.100)의 80번 포트로 접근했을 때, 실제로는 내부의 다른 서버 (예: 192.168.0.101)의 8080번 포트로 연결되도록 목적지를 변경하는 경우.
> 
> 2. POSTROUTING 체인 
> 시점: 패킷이 리눅스 서버의 네트워크 인터페이스를 통해 나가기 직전에 적용됩니다. 라우팅 결정이 이루어진 후입니다.
> 주요 용도: Source NAT (SNAT). 즉, 패킷의 출발지 IP 주소와 포트를 변경할 때 사용합니다. MASQUERADE는 SNAT의 특수한 형태로, 동적으로 나가는 인터페이스의 IP 주소를 출발지 주소로 사용합니다.
> 예시: 내부 네트워크의 서버들이 외부 인터넷으로 나갈 때, 내부 IP 주소가 아닌 DMZ 서버의 공인 IP로 위장하여 나가는 경우. 혹은 당신의 시나리오처럼 DMZ 서버를 통해 내부 서버로 포워딩된 트래픽이, 내부 서버에서 응답을 보낼 때 DMZ 서버를 통해 원래 클라이언트에게 돌아올 수 있도록 출발지 IP를 변경하는 경우.
> 
> 즉, `PREROUTING`은 들어오는 패킷의 목적지 변경, `POSTROUTING`은 나가는 패킷의 출발지 변경 작업이 된다.
> 양쪽 모두가 변경되어야 상대 서버와 정상적인 통신이 가능해진다.

## 3. 포트포워딩 설정 저장

서버 재부팅 시에도 유지되도록 설정을 저장해야 한다. 저장을 위해서는 다른 패키지가 필요하다.

```shell
sudo apt install iptables-persistent
```

규칙 저장은 아래와 같다.

```shell
sudo netfilter-persistent save
```

## 4. 서버의 방화벽이 있다면

명령어를 허용해서 인바운드 트래픽을 허용해주어야 한다. 

```shell
sudo ufw allow [EXTERNAL_PORT]/tcp <- 열어줄 대상 포트
sudo ufw reload
```

