# CatHunt 서버 배포: 이미지 빌드 → 서버로 직접 전송(docker save/load, 레지스트리 불필요) → 재기동
# 사전: Docker Desktop 실행, `terraform apply` 완료(서버·키 생성됨)
# 예: .\deploy.ps1 -ServerIp 1.2.3.4
param(
  [Parameter(Mandatory = $true)][string]$ServerIp,
  [string]$ImageTag = "cathunt:latest",
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
