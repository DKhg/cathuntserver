# CatHunt 운영 인프라 (AWS, EC2 + Docker Compose)
# - EC2 1대에서 docker compose 로 caddy(HTTPS) + app(Spring Boot) + db(Postgres) 를 돌린다.
# - 앱 이미지는 ECR 에 올리고 박스가 IAM 역할로 pull 한다.
# - 비밀값(DB 비번·JWT 시크릿)과 SSH 키는 Terraform 이 만들어 주입한다. 커밋에는 안 들어간다.
# 적용: terraform init && terraform apply   (AWS 자격증명 + domain_name 변수 필요)

terraform {
  required_version = ">= 1.5"
  required_providers {
    aws    = { source = "hashicorp/aws", version = "~> 5.0" }
    tls    = { source = "hashicorp/tls", version = "~> 4.0" }
    random = { source = "hashicorp/random", version = "~> 3.0" }
    local  = { source = "hashicorp/local", version = "~> 2.0" }
  }
}

provider "aws" {
  region = var.aws_region
}

# 실제 사용할 도메인: 입력하면 그 도메인, 비우면 서버 IP 기반 sslip.io 무료 주소(DNS 불필요)
locals {
  effective_domain = var.domain_name != "" ? var.domain_name : "${replace(aws_eip.this.public_ip, ".", "-")}.sslip.io"
}

# --- SSH 키 (로컬에 .pem 저장, git 제외) ---
resource "tls_private_key" "ssh" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "this" {
  key_name   = "${var.project}-key"
  public_key = tls_private_key.ssh.public_key_openssh
}

resource "local_sensitive_file" "pem" {
  content         = tls_private_key.ssh.private_key_pem
  filename        = "${path.module}/${var.project}-key.pem"
  file_permission = "0600"
}

# --- 비밀값 자동 생성 (DB 비번 24자, JWT 시크릿 48자, 특수문자 없음=env/yaml 안전) ---
resource "random_password" "db" {
  length  = 24
  special = false
}

resource "random_password" "jwt" {
  length  = 48
  special = false
}

# (레지스트리 없음) 이미지는 로컬에서 빌드해 deploy.ps1 이 `docker save | ssh docker load`로 서버에 직접 넣는다.
# 그래서 ECR·IAM 권한이 필요 없다(비용 0).

# --- 최신 Amazon Linux 2023 (x86_64) AMI ---
# SSM 대신 EC2 DescribeImages로 조회(= AmazonEC2FullAccess 만으로 가능, SSM 권한 불필요)
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
  filter {
    name   = "architecture"
    values = ["x86_64"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
  filter {
    name   = "state"
    values = ["available"]
  }
}

# --- 보안 그룹: SSH(제한 권장) + HTTP/HTTPS(공개). DB 5432 는 열지 않음(컨테이너 내부 전용) ---
resource "aws_security_group" "this" {
  name        = "${var.project}-sg"
  description = "CatHunt server: SSH, HTTP(ACME), HTTPS"

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }
  ingress {
    description = "HTTP (ACME challenge and redirect)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Project = var.project }
}

# --- EC2 인스턴스 (부팅 시 user_data 로 Docker 설치 + compose 파일 작성 + db/caddy 기동) ---
resource "aws_instance" "this" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.this.key_name
  vpc_security_group_ids = [aws_security_group.this.id]

  user_data = templatefile("${path.module}/user_data.sh", {
    db_password = random_password.db.result
    jwt_secret  = random_password.jwt.result
    domain      = local.effective_domain
    image       = var.app_image # ghcr 이미지(ghcr.io/dkhg/cathunt). compose에서 :latest 붙여 pull
  })

  root_block_device {
    volume_size = var.disk_gb
    volume_type = "gp3"
    encrypted   = true
  }

  tags = { Name = "${var.project}-server", Project = var.project }
}

# --- 고정 공인 IP ---
# 인스턴스보다 먼저 할당해야 그 IP로 sslip.io 도메인을 만들어 user_data에 넣을 수 있다.
# (그래서 instance 연결은 아래 association 으로 분리)
resource "aws_eip" "this" {
  domain = "vpc"
  tags   = { Project = var.project }
}

resource "aws_eip_association" "this" {
  instance_id   = aws_instance.this.id
  allocation_id = aws_eip.this.id
}
