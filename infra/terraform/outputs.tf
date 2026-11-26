output "server_ip" {
  description = "서버 고정 공인 IP (DNS A 레코드가 가리킬 주소)"
  value       = aws_eip.this.public_ip
}

output "ssh_command" {
  description = "SSH 접속 명령"
  value       = "ssh -i ${abspath(local_sensitive_file.pem.filename)} ec2-user@${aws_eip.this.public_ip}"
}

output "db_password" {
  description = "자동 생성된 DB 비번 (참고용)"
  value       = random_password.db.result
  sensitive   = true
}

output "jwt_secret" {
  description = "자동 생성된 JWT 시크릿 (참고용)"
  value       = random_password.jwt.result
  sensitive   = true
}

output "server_url" {
  description = "서버 HTTPS 주소 (sslip.io 자동 또는 입력 도메인)"
  value       = "https://${local.effective_domain}"
}

output "next_steps" {
  value = <<-EOT

    === 다음 순서 ===
    서버 주소: https://${local.effective_domain}   (sslip.io 자동 — DNS 설정 불필요)
    1) 첫 배포(이미지 빌드 → 서버로 전송 → 기동):
       powershell infra\deploy\deploy.ps1 -ServerIp ${aws_eip.this.public_ip}
    2) 확인:  curl https://${local.effective_domain}/master   (JSON 나오면 성공)
       (HTTPS 인증서 자동발급에 1~2분 걸릴 수 있음)
    3) Unity ServerConfig 운영주소 = https://${local.effective_domain}  (Claude에게 "운영주소 연결해줘")
  EOT
}
