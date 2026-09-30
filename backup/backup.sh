#!/bin/bash
# ============================================================
# Backup automatico de la base de datos de El Puntazo.
# Genera un archivo comprimido con fecha y elimina los que
# tengan mas de 30 dias para no llenar el disco.
#
# Uso manual:   ./backup/backup.sh
# Uso programado: ver DEPLOYMENT.md, seccion de backups (cron).
# ============================================================
set -e

cd "$(dirname "$0")/.."

# Carga las variables del .env (necesitamos DB_ROOT_PASSWORD)
set -a
source .env
set +a

FECHA=$(date +%Y-%m-%d_%H-%M-%S)
ARCHIVO="backup/elpuntazo_${FECHA}.sql.gz"

mkdir -p backup

docker compose exec -T mysql sh -c "mysqldump -uroot -p\"$DB_ROOT_PASSWORD\" elpuntazo" | gzip > "$ARCHIVO"

echo "Backup creado: $ARCHIVO"

# Elimina backups locales de mas de 30 dias
find backup -name "elpuntazo_*.sql.gz" -mtime +30 -delete

echo "Backups antiguos (mas de 30 dias) eliminados."
