#!/bin/bash
# ============================================================
# Restaura la base de datos desde un archivo de backup.
#
# Uso: ./backup/restore.sh backup/elpuntazo_2026-01-15_03-00-00.sql.gz
#
# ADVERTENCIA: esto reemplaza los datos actuales de la base de
# datos con los del backup. Usalo solo si sabes lo que haces,
# por ejemplo tras un problema serio del servidor.
# ============================================================
set -e

if [ -z "$1" ]; then
  echo "Debes indicar el archivo de backup a restaurar."
  echo "Ejemplo: ./backup/restore.sh backup/elpuntazo_2026-01-15_03-00-00.sql.gz"
  exit 1
fi

cd "$(dirname "$0")/.."

set -a
source .env
set +a

echo "Vas a restaurar el backup: $1"
echo "Esto reemplazara los datos actuales. Escribe SI para continuar:"
read -r CONFIRMACION

if [ "$CONFIRMACION" != "SI" ]; then
  echo "Cancelado."
  exit 0
fi

gunzip -c "$1" | docker compose exec -T mysql sh -c "mysql -uroot -p\"$DB_ROOT_PASSWORD\" elpuntazo"

echo "Restauracion completada."
