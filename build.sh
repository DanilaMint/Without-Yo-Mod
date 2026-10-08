#!/usr/bin/env bash
# build.sh — собрать withoutyo mod для выбранных загрузчиков.
#
# Использование:
#   ./build.sh                    # собрать fabric, forge, neoforge
#   ./build.sh neoforge           # собрать только neoforge
#   ./build.sh neoforge forge     # собрать neoforge и forge
#
# Скрипт для каждого loader'а создаёт временный settings.gradle, чтобы Gradle
# не пытался сконфигурировать соседние subprojects (Fabric Loom 1.7-SNAPSHOT
# ломается на Gradle 8.14+ во время configure, что валит весь корневой билд).

set -uo pipefail

GRADLE_CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists"
LOADERS_DEFAULT=("fabric" "forge" "neoforge")

if [ $# -eq 0 ]; then
    LOADERS=("${LOADERS_DEFAULT[@]}")
else
    LOADERS=("$@")
fi

GRADLE_BIN=$(ls -d "$GRADLE_CACHE"/gradle-*-bin/*/gradle-*/bin/gradle 2>/dev/null | sort -V | tail -1)
if [ -z "$GRADLE_BIN" ] || [ ! -x "$GRADLE_BIN" ]; then
    echo "Error: Gradle не найден в $GRADLE_CACHE/gradle-*-bin/*/gradle-*/bin/gradle"
    echo "Сначала запустите любой gradle wrapper или установите gradle, чтобы заполнить кэш."
    exit 1
fi

GRADLE_VERSION=$(basename "$(dirname "$(dirname "$GRADLE_BIN")")")
ROOT="$(cd "$(dirname "$0")" && pwd)"

declare -A RESULTS
OVERALL=0

build_loader() {
    local loader="$1"
    local loader_dir="$ROOT/$loader"
    local settings="$loader_dir/settings.gradle"
    local backup="$settings.bak"

    if [ ! -d "$loader_dir" ]; then
        echo "=== $loader: пропуск (нет директории $loader_dir) ==="
        RESULTS[$loader]="skipped"
        return
    fi
    if [ ! -f "$loader_dir/build.gradle" ]; then
        echo "=== $loader: пропуск (нет build.gradle) ==="
        RESULTS[$loader]="skipped"
        return
    fi

    if [ -f "$settings" ]; then
        cp "$settings" "$backup"
    fi

    cat > "$settings" <<EOF
rootProject.name = 'withoutyo'
include '$loader'
EOF

    echo "================================================"
    echo "=== Building $loader (Gradle $GRADLE_VERSION)"
    echo "================================================"

    cd "$loader_dir"
    if "$GRADLE_BIN" clean jar --no-daemon; then
        RESULTS[$loader]="OK"
        local jar
        jar=$(ls -1 "$loader_dir/build/libs/"*.jar 2>/dev/null | head -1)
        if [ -n "$jar" ]; then
            echo "=== $loader: JAR → $jar"
        fi
    else
        RESULTS[$loader]="FAILED"
        OVERALL=1
        echo "=== $loader: СБОРКА ПРОВАЛЕНА ==="
    fi

    cd "$ROOT"
    if [ -f "$backup" ]; then
        mv "$backup" "$settings"
    else
        rm -f "$settings"
    fi
}

for loader in "${LOADERS[@]}"; do
    build_loader "$loader"
done

echo ""
echo "================================================"
echo "=== Build summary"
echo "================================================"
for loader in "${LOADERS[@]}"; do
    status="${RESULTS[$loader]:-skipped}"
    echo "  $loader: $status"
done

exit $OVERALL