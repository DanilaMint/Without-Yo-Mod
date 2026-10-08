# install.sh — деплой собранного JAR в инстанс PrismLauncher

**Дата:** 2026-10-08
**Статус:** дизайн согласован с пользователем

## Цель

Копировать свежесобранный JAR мода `withoutyo` в директорию `mods/` инстанса PrismLauncher, чтобы не делать это вручную после каждой сборки.

## Область

- Внутри: bash-скрипт `install.sh` в корне проекта + строка в `.gitignore`.
- Снаружи: `build.sh` остаётся как есть; никаких хуков, watcher'ов или автозапуска.

## API

```
./install.sh                 # деплоит neoforge (default)
./install.sh neoforge        # явный выбор
```

Другие loader'ы (fabric, forge) — не запрашивались, будут добавлены раскомментированием строк в `LOADER_TARGET` при появлении соответствующих инстансов.

## Алгоритм для одного loader'а

1. `src_root="$ROOT/$loader"`.
2. Найти первый `*.jar` в `$src_root/build/libs/`, исключая `*-sources.jar`.
3. Если JAR не найден — `RESULTS[$loader]="NO_BUILT_JAR"`, вывести подсказку «запустите `./build.sh $loader` сначала».
4. `mkdir -p` целевой директорию.
5. `cp -f "$src_jar" "$target_dir/"`, залогировать.
6. `RESULTS[$loader]="OK"`.

## Конфигурация

В начале скрипта:

```bash
declare -A LOADER_TARGET=(
    [neoforge]="$HOME/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/no-yo-mod-neoforge/minecraft/mods"
    # [fabric]="..."
    # [forge]="..."
)
DEFAULT_LOADERS=(neoforge)
```

`$HOME` — намеренно, чтобы путь был переносим между машинами одного пользователя и не падал, если у других контрибьюторов другой `$HOME`.

## Обработка ошибок

- `set -uo pipefail` (как в `build.sh`).
- Без `set -e` — чтобы цикл по loader'ам не прерывался на первой ошибке.
- Итоговая сводка: для каждого loader'а вывести `OK` / `NO_BUILT_JAR` / `FAILED`.
- Exit-код `0` если все OK, `1` иначе.

## Что скрипт НЕ делает

- Не запускает `./build.sh` сам — разные ответственности.
- Не очищает целевую `mods/` директорию.
- Не удаляет старые JAR'ы с устаревшими версиями.
- Не заходит в `common/`, `forge/`, `fabric/` — только артефакты `<loader>/build/libs/`.
- Не делает ничего за пределами своей конфигурации.

## .gitignore

Добавить одну строку:

```
install.sh
```

Причина: путь к инстансу PrismLauncher (`/home/danila/.var/app/...`) привязан к локальной машине пользователя. У других контрибьюторов этого пути не будет — скрипт у них не сработает.

## Проверка (ручная)

1. `./build.sh neoforge` — собрать.
2. `./install.sh` — задеплоить.
3. `ls -la ~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/no-yo-mod-neoforge/minecraft/mods/` — убедиться, что `withoutyo-*.jar` на месте и обновлён.
4. Запустить инстанс в Prism — мод загрузился.