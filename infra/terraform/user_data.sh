#!/bin/bash
# EC2 첫 부팅 시 1회 실행. Docker 설치 → compose 파일/비밀값 작성 → db·caddy 기동.
# 앱 이미지는 아직 ECR에 없으므로, 첫 배포(deploy.ps1)에서 push 후 app 컨테이너가 뜬다.
# 주의: 이 파일은 Terraform templatefile 이 먼저 치환한다(대괄호 변수). bash 변수는 반드시 중괄호 없이 $VAR 로 쓴다.
set -euxo pipefail

# --- 스왑 2GB (t3.micro 1GB 메모리 보완) ---
if [ ! -f /swapfile ]; then
  dd if=/dev/zero of=/swapfile bs=1M count=2048
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

# --- Docker 설치 ---
dnf install -y docker
systemctl enable --now docker
usermod -aG docker ec2-user || true

# --- docker compose v2 플러그인 ---
mkdir -p /usr/local/lib/docker/cli-plugins
curl -fsSL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# --- 앱 디렉토리 ---
mkdir -p /opt/cathunt

# --- Caddyfile (도메인 넣으면 HTTPS 인증서 자동 발급·갱신) ---
cat > /opt/cathunt/Caddyfile <<CADDY
${domain} {
    reverse_proxy app:8090
}
CADDY

# --- docker-compose.yml (비밀값은 Terraform이 주입. 파일 권한 600) ---
cat > /opt/cathunt/docker-compose.yml <<COMPOSE
services:
  db:
    image: postgres:15
    restart: always
    environment:
      POSTGRES_USER: hong
      POSTGRES_PASSWORD: "${db_password}"
      POSTGRES_DB: cathunt
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hong -d cathunt"]
      interval: 10s
      timeout: 5s
      retries: 10

  app:
    image: ${image}:latest
    restart: always
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://db:5432/cathunt
      DB_USER: hong
      DB_PASSWORD: "${db_password}"
      JWT_SECRET: "${jwt_secret}"
      SERVER_PORT: "8090"
      # 힙을 512MB로 고정(앱이 메모리 폭주로 db를 OOM-kill 하는 것 방지). t3.micro(1GB)로 바꾸면 384m 권장
      JAVA_TOOL_OPTIONS: "-Xmx512m -XX:+UseSerialGC"
    mem_limit: 768m

  caddy:
    image: caddy:2
    restart: always
    depends_on:
      - app
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /opt/cathunt/Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy_data:/data
      - caddy_config:/config

volumes:
  pgdata:
  caddy_data:
  caddy_config:
COMPOSE
chmod 600 /opt/cathunt/docker-compose.yml

# --- db·caddy 먼저 기동 (app 이미지는 첫 배포 때 deploy.ps1 이 docker load 로 넣는다) ---
cd /opt/cathunt
docker compose up -d db caddy
