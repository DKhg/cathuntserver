variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2" # 서울
}

variable "project" {
  description = "리소스 이름 접두사 / ECR 저장소 이름"
  type        = string
  default     = "cathunt"
}

variable "instance_type" {
  description = "EC2 타입. 안정(권장)=t3.small(2GB). 최소비용/프리티어=t3.micro(1GB, 스왑 의존·OOM 위험)"
  type        = string
  default     = "t3.small"
}

variable "disk_gb" {
  description = "루트 디스크 크기(GB). 프리티어는 30GB까지 무료"
  type        = number
  default     = 20
}

variable "app_image" {
  description = "앱 컨테이너 이미지 (ghcr, 태그 제외). GitHub 오너 소문자. 예: ghcr.io/dkhg/cathunt"
  type        = string
  default     = "ghcr.io/dkhg/cathunt"
}

variable "domain_name" {
  description = "서버 도메인 (예: api.example.com). 비우면 서버 IP 기반 무료 주소(sslip.io) 자동 사용 — DNS 설정 불필요"
  type        = string
  default     = ""
}

variable "allowed_ssh_cidr" {
  description = "SSH 허용 대역. 내 공인 IP/32 권장 (예: 1.2.3.4/32)"
  type        = string
  default     = "0.0.0.0/0"
}
