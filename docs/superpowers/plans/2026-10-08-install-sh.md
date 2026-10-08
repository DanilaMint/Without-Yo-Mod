# install.sh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deploy built `withoutyo` JAR to a PrismLauncher instance `mods/` directory via a small manual bash script.

**Architecture:** A `install.sh` in the project root, mirroring the style of the existing `build.sh` (same `set -uo pipefail`, summary-then-exit pattern, no `-e`). An associative array `LOADER_TARGET` maps loader names to PrismLauncher instance dirs. The script iterates over the requested loaders (default: just `neoforge`), picks the first non-`*-sources.jar` from `<loader>/build/libs/`, copies it with `cp -f`, and prints a summary.

**Tech Stack:** Bash 5+, GNU coreutils (`ls`, `cp`, `mkdir`, `grep`). No external dependencies, no test framework.

## Global Constraints

- Project root: `/home/danila/projects/minecraft/no-yo-mod` (resolved via `$(cd "$(dirname "$0")" && pwd)`).
- Bash style mirrors `build.sh`: `#!/usr/bin/env bash`, `set -uo pipefail`, no `set -e`, declare-then-loop with summary at the end.
- `.gitignore` lives at the project root.
- Git is **not** initialized in this project as of 2026-10-08. Task 1 initializes it.
- The PrismLauncher path embeds `$HOME` (not a hardcoded `/home/danila/...`) so the script is portable across machines for the same user.
- No automated test framework — Task 3 verifies behavior manually, matching the project's existing verification style (see `build.sh`, which also has no automated tests).

---

## File Structure

| File | Action | Purpose |
|---|---|---|
| `install.sh` | Create | Deploy script. Same style and CLI shape as `build.sh`. |
| `.gitignore` | Modify | Add `install.sh` line so the script stays local. |
| `.git/` | Create (via Task 1) | Repository initialization. |

---

### Task 1: Initialize git repository with baseline commit

**Files:**
- Create: `.git/` (via `git init`)
- Create: initial commit covering the current project state (excluding ignored files)

**Why first:** `.gitignore` already exists in the project; making it functional requires a repo. Capturing a baseline commit before adding `install.sh` makes future `git log`/`git blame` interpretable.

- [ ] **Step 1: Verify no repo exists, then initialize**

Run:
```bash
cd /home/danila/projects/minecraft/no-yo-mod
git rev-parse --show-toplevel 2>/dev/null && echo "REPO_EXISTS" || echo "NO_REPO"
git init
```
Expected: First command prints `NO_REPO` (and exits non-zero — that's fine, `||` catches it). Second command prints `Initialized empty Git repository in /home/danila/projects/minecraft/no-yo-mod/.git/`.

- [ ] **Step 2: Configure user identity if missing**

Run:
```bash
git config user.name
git config user.email
```
If both print non-empty values, skip to Step 3. Otherwise set them (replace with real values; this is local config only, not global):
```bash
git config user.name "Your Name"
git config user.email "you@example.com"
```

- [ ] **Step 3: Stage current project files**

Run:
```bash
git add .
git status --short | head -30
```
Expected: Files listed are exactly the source/build configuration. The `.gitignore` already excludes `*.jar` (with allow-listed exceptions for build/libs/*.jar under each loader — but those JARs are NOT created yet during a fresh clone, so they won't appear either way), `build/`, `out/`, `.gradle/`, `.idea/`, `.vscode/`, IDE files, logs, OS junk. Verify no large unexpected files appear (no `node_modules`, no stray JARs from `~/.gradle`).

- [ ] **Step 4: Create baseline commit**

Run:
```bash
git commit -m "chore: initial commit (baseline)"
```
Expected: Commit succeeds. `git log --oneline` shows one commit.

- [ ] **Step 5: Verify baseline is reproducible**

Run:
```bash
git status
```
Expected: `nothing to commit, working tree clean`.

---

### Task 2: Add `install.sh` and update `.gitignore`

**Files:**
- Create: `/home/danila/projects/minecraft/no-yo-mod/install.sh`
- Modify: `/home/danila/projects/minecraft/no-yo-mod/.gitignore`

**Interfaces:**
- Consumes: nothing (first task to produce something).
- Produces: an executable `install.sh` that the next task verifies.

- [ ] **Step 1: Create `install.sh` with the following content**

Write the file `/home/danila/projects/minecraft/no-yo-mod/install.sh` with this exact content:

```bash
#!/usr/bin/env bash
# install.sh — скопировать собранный withoutyo JAR в mods/ инстанса PrismLauncher.
#
# Использование:
#   ./install.sh                 # деплоит neoforge (по умолчанию)
#   ./install.sh neoforge        # то же самое, явный выбор
#
# Источник: <loader>/build/libs/*.jar (первый, не -sources).
# Стиль зеркалирует build.sh: set -uo pipefail, без -e, итоговая сводка.

set -uo pipefail

# Расширяйте по мере появления новых инстансов PrismLauncher.
declare -A LOADER_TARGET=(
    [neoforge]="$HOME/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/no-yo-mod-neoforge/minecraft/mods"
    # [fabric]="$HOME/.var/app/.../no-yo-mod-fabric/minecraft/mods"
    # [forge]="$HOME/.var/app/.../no-yo-mod-forge/minecraft/mods"
)
DEFAULT_LOADERS=(neoforge)

if [ $# -eq 0 ]; then
    LOADERS=("${DEFAULT_LOADERS[@]}")
else
    LOADERS=("$@")
fi

ROOT="$(cd "$(dirname "$0")" && pwd)"

declare -A RESULTS
OVERALL=0

install_loader() {
    local loader="$1"
    local target="${LOADER_TARGET[$loader]:-}"

    if [ -z "$target" ]; then
        echo "=== $loader: пропуск (нет маппинга в LOADER_TARGET)"
        RESULTS[$loader]="NO_TARGET"
        OVERALL=1
        return
    fi

    local src
    src=$(ls -1 "$ROOT/$loader/build/libs/"*.jar 2>/dev/null \
        | grep -v -- '-sources\.jar$' \
        | head -1)

    if [ -z "$src" ]; then
        echo "=== $loader: НЕТ JAR в $ROOT/$loader/build/libs/"
        echo "    Сначала запустите: ./build.sh $loader"
        RESULTS[$loader]="NO_BUILT_JAR"
        OVERALL=1
        return
    fi

    mkdir -p "$target"
    if cp -f "$src" "$target/"; then
        echo "=== $loader: $src -> $target/"
        RESULTS[$loader]="OK"
    else
        echo "=== $loader: КОПИРОВАНИЕ НЕ УДАЛОСЬ"
        RESULTS[$loader]="FAILED"
        OVERALL=1
    fi
}

for loader in "${LOADERS[@]}"; do
    install_loader "$loader"
done

echo ""
echo "================================================"
echo "=== Install summary"
echo "================================================"
for loader in "${LOADERS[@]}"; do
    status="${RESULTS[$loader]:-skipped}"
    echo "  $loader: $status"
done

exit $OVERALL
```

Notes for the implementer:
- Keep the `LOADER_TARGET` mapping commented examples — they show the pattern for future extension without forcing the path to be invented now.
- `set -uo pipefail` matches `build.sh` exactly. No `-e` — the loop must continue across loaders.

- [ ] **Step 2: Add `install.sh` to `.gitignore`**

Open `.gitignore` and add a new section at the bottom (after `# Логи и прочее`):

```gitignore
# Локальные скрипты
install.sh
```

If the file already has different section ordering, place the two lines at the end with a blank line separating them from the previous block.

- [ ] **Step 3: Make `install.sh` executable**

Run:
```bash
chmod +x /home/danila/projects/minecraft/no-yo-mod/install.sh
ls -l /home/danila/projects/minecraft/no-yo-mod/install.sh
```
Expected: Permissions show `-rwxr-xr-x`.

- [ ] **Step 4: Verify `install.sh` is ignored, `.gitignore` is tracked**

Run:
```bash
cd /home/danila/projects/minecraft/no-yo-mod
git status --short
```
Expected: only `.gitignore` appears as a modification. `install.sh` must NOT appear anywhere in the output (it's ignored).

If `install.sh` shows up:
- Check that the `.gitignore` line is exactly `install.sh` (not `install.sh/` or with a wildcard).
- Check that the file isn't already tracked: `git ls-files install.sh` should print nothing.
- If tracked: `git rm --cached install.sh` (the working tree file stays, just untracked it).

- [ ] **Step 5: Commit the `.gitignore` change**

Run:
```bash
cd /home/danila/projects/minecraft/no-yo-mod
git add .gitignore
git commit -m "chore: ignore local install.sh"
```
Expected: Commit succeeds. `git status` clean. `git log --oneline` shows two commits.

---

### Task 3: Manual end-to-end verification

**Why:** No test framework in the project — `build.sh` is verified the same way (manual run, eyeball the output). Mirror that style.

**Files:** none (only runs the script and reads output).

- [ ] **Step 1: Confirm a built JAR exists**

Run:
```bash
ls /home/danila/projects/minecraft/no-yo-mod/neoforge/build/libs/*.jar
```
Expected: at least one JAR listed. If empty, run `./build.sh neoforge` from the project root first (this can take minutes on first run because Gradle downloads dependencies).

- [ ] **Step 2: Run default invocation (no args)**

Run:
```bash
cd /home/danila/projects/minecraft/no-yo-mod
./install.sh
echo "exit=$?"
```
Expected output contains:
- A line `=== neoforge: /home/.../neoforge/build/libs/withoutyo-*.jar -> /home/danila/.var/.../no-yo-mod-neoforge/minecraft/mods/`
- A summary block `=== Install summary` with `  neoforge: OK`
- `exit=0`

- [ ] **Step 3: Verify the JAR landed in PrismLauncher**

Run:
```bash
ls -la /home/danila/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/no-yo-mod-neoforge/minecraft/mods/
```
Expected: `withoutyo-1.21.1-neoforge-1.0.0.jar` (or whatever the current build produced) is present, mtime updated to "now".

- [ ] **Step 4: Verify NO_BUILT_JAR failure path**

Run:
```bash
cd /home/danila/projects/minecraft/no-yo-mod
mv neoforge/build/libs /tmp/no-yo-backup-libs
./install.sh neoforge
echo "exit=$?"
mv /tmp/no-yo-backup-libs neoforge/build/libs
```
Expected output contains:
- `=== neoforge: НЕТ JAR в /home/.../neoforge/build/libs/`
- `    Сначала запустите: ./build.sh neoforge`
- Summary shows `  neoforge: NO_BUILT_JAR`
- `exit=1`

Verify the libs directory is back where it was (`ls neoforge/build/libs/*.jar` shows the JAR).

- [ ] **Step 5: Restore working state**

Run:
```bash
cd /home/danila/projects/minecraft/no-yo-mod
./install.sh
ls /home/danila/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/no-yo-mod-neoforge/minecraft/mods/
```
Expected: Re-install succeeds; the JAR in the PrismLauncher mods dir is back. (Optional but recommended: open the instance in Prism and confirm the mod loads — but that's a UI step the implementer can skip if it bloats their loop.)

---

## Self-Review

**Spec coverage:**
- "API": `./install.sh` and `./install.sh neoforge` → Tasks 2+3 cover both.
- "Algorithm for one loader": find JAR excluding sources, error if missing, mkdir, copy, log → Task 2 step 1.
- "Configuration" (`LOADER_TARGET`, `DEFAULT_LOADERS`) → Task 2 step 1.
- "Error handling" (`set -uo pipefail`, summary-then-exit, exit codes) → Task 2 step 1.
- "What the script does NOT do" → enforced by absence (no `build.sh` invocation, no cleanup logic, no symlink tricks in the script body).
- ".gitignore": `install.sh` line → Task 2 step 2.
- "Verification" — manual run through OK and NO_BUILT_JAR paths → Task 3.

**Placeholder scan:** no TBD/TODO/"similar to"/vague phrasing. The full `install.sh` body is provided verbatim in Task 2 step 1.

**Type/signature consistency:** the script defines `LOADER_TARGET` and `DEFAULT_LOADERS` in Task 2 step 1 and uses them consistently in the same file. No cross-task name mismatches.

**Scope check:** one script + one gitignore line + one `git init`. All in one plan, all under ~15 steps. Appropriate.

**Ambiguity check:**
- "first non-sources JAR" is unambiguous: `ls ... | grep -v -- '-sources\.jar$' | head -1`.
- "exit 1 if any loader failed" is unambiguous: `OVERALL=1` accumulates, single `exit $OVERALL` at end.
- "ignored" vs "untracked": step 4 explicitly handles the edge case where someone accidentally committed the file before adding the gitignore line.