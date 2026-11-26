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

output "next_steps" {
  value = <<-EOT

    === 다음 순서 ===
    1) DNS A 레코드 등록:  ${var.domain_name}  ->  ${aws_eip.this.public_ip}
    2) 첫 배포(이미지 빌드 → 서버로 전송 → 기동):
       powershell infra\deploy\deploy.ps1 -ServerIp ${aws_eip.this.public_ip}
    3) 확인:  curl https://${var.domain_name}/master   (JSON 나오면 성공)
    4) Unity ServerConfig 운영주소 = https://${var.domain_name}  (Claude에게 "운영주소 연결해줘")
  EOT
}
