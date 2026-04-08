#!/usr/bin/env bash
set -euo pipefail

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
cd "$script_dir"

main_class="com.meerkat.smashameerkat.SmashAMeerkatApplication"

find_existing_pids() {
  MAIN_CLASS="$main_class" powershell.exe -NoProfile -Command \
    '$mainClass = $env:MAIN_CLASS; Get-CimInstance Win32_Process -Filter "Name='"'"'java.exe'"'"'" | Where-Object { $_.CommandLine -like "*$mainClass*" } | Select-Object -ExpandProperty ProcessId' \
    | tr -d '\r'
}

stop_existing_app() {
  existing_pids="$(find_existing_pids || true)"
  [ -n "$existing_pids" ] || return 0

  printf 'Stopping existing %s instance(s): %s\n' "$main_class" "$existing_pids"
  while IFS= read -r pid; do
    [ -n "$pid" ] || continue
    taskkill.exe //PID "$pid" //F >/dev/null
  done <<EOF
$existing_pids
EOF
}

stop_existing_app
exec ./mvnw -Dmaven.test.skip=true spring-boot:run "$@"
