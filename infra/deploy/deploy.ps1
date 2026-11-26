# CatHunt 서버 수동 배포(백업 수단). 평소엔 git push → GitHub Actions 자동배포(ghcr) 사용.
# 이건 레지스트리 없이 로컬 빌드→save/load 로 서버에 직접 넣는다(이미지 태그는 compose와 동일해야 함).
# 사전: Docker Desktop 실행, `terraform apply` 완료(서버·키 생성됨)
# 예: .\deploy.ps1 -ServerIp 1.2.3.4
param(
  [Parameter(Mandatory = $true)][string]$ServerIp,
  [string]$ImageTag = "ghcr.io/dkhg/cathunt:latest",
  [string]$KeyPath = "$PSScriptRoot\..\terraform\cathunt-key.pem",
  [string]$User = "ec2-user"
)
$ErrorActionPreference = "Stop"
$repo = (Resolve-Path "$PSScriptRoot\..\..").Path
$tar = Join-Path $env:TEMP "cathunt-image.tar"

Write-Host "[1/4] 이미지 빌드 ($ImageTag)"
docker build -t $ImageTag $repo

Write-Host "[2/4] 이미지 파일로 저장 → 서버로 전송 (레지스트리 없이 직접)"
# PowerShell 파이프는 바이너리를 깨뜨릴 수 있어 파일로 저장 후 scp 한다
docker save $ImageTag -o $tar
scp -o StrictHostKeyChecking=accept-new -i $KeyPath $tar "$User@${ServerIp}:/tmp/cathunt-image.tar"

Write-Host "[3/4] 서버에서 이미지 적재 + 재기동"
ssh -i $KeyPath "$User@$ServerIp" "sudo docker load -i /tmp/cathunt-image.tar && rm -f /tmp/cathunt-image.tar && cd /opt/cathunt && sudo docker compose up -d"

Write-Host "[4/4] 상태"
ssh -i $KeyPath "$User@$ServerIp" "sudo docker compose -f /opt/cathunt/docker-compose.yml ps"
Remove-Item $tar -ErrorAction SilentlyContinue
Write-Host "완료. 잠시 뒤  curl https://<도메인>/master  로 확인하세요."
