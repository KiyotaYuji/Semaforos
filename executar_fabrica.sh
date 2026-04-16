#!/usr/bin/env bash
set -e
BASE_DIR="$(dirname "$0")"
"$BASE_DIR/compilar.sh"
cd "$BASE_DIR/bin"
exec java fabrica.Fabrica
