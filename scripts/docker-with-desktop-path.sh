#!/usr/bin/env bash
# Use when `docker compose` fails with:
#   error getting credentials … docker-credential-desktop … not found in $PATH
# Homebrew's docker CLI often does not include Docker Desktop's bin directory, where
# docker-credential-desktop lives (see ~/.docker/config.json credsStore: desktop).
#
# Prepends Docker Desktop's Resources/bin to PATH so the credential helper is found.
# Uses Homebrew's docker when present (/opt/homebrew or /usr/local) so `docker compose`
# still resolves the Compose v2 CLI plugin from ~/.docker/cli-plugins — putting Docker.app's
# docker first on PATH would shadow Homebrew and break `docker compose`.
set -eu
docker_bin=""
for c in /opt/homebrew/bin/docker /usr/local/bin/docker; do
  if [[ -x "$c" ]]; then
    docker_bin="$c"
    break
  fi
done
if [[ -z "${docker_bin}" ]]; then
  docker_bin="$(command -v docker)"
fi
if [[ "$(uname -s)" == "Darwin" ]] && [[ -d "/Applications/Docker.app/Contents/Resources/bin" ]]; then
  export PATH="/Applications/Docker.app/Contents/Resources/bin:${PATH:-}"
fi
exec "${docker_bin}" "$@"
