# Without Ё patch

**Нормализует русский текст «ё»→«е» в Minecraft и поиске JEI.**

Пользователь, который печатает в поиске creative inventory слово «черная» (без ё), находит
предметы с именем «Чёрная шерсть». То же самое работает в любую сторону: и «чёрная», и
«чёрная шерсть» становятся эквивалентны «черная» / «черная шерсть» для движка поиска.

Поведение сохраняется для обоих направлений ввода благодаря тому, что мод нормализует
**обе стороны сравнения** — и запрос, и индексированные строки.

---

## Совместимость

| Загрузчик  | Версии MC          | Готовые jar'ы                                           |
|-----------|--------------------|---------------------------------------------------------|
| Fabric    | 1.20.1 — 1.21.x    | `withoutyo-1.20.1-fabric.jar`, `withoutyo-1.21-fabric.jar` |
| Forge     | 1.20.1 (47.3+)     | `withoutyo-1.20.1-forge.jar`                            |
| NeoForge  | 1.21.x             | `withoutyo-1.21-neoforge.jar`                           |

JEI поддерживается как **опциональная зависимость** — мод запускается без JEI,
а при наличии JEI расширяет и его поиск.

> Если нужны дополнительные версии MC (1.20.4-fabric, 1.20.6-forge и т.д.), см. раздел
> [«Как добавить новую версию»](#как-добавить-новую-версию) ниже.

---

## Структура проекта

```
no-yo-mod/
├── settings.gradle              # объявляет subprojects: fabric, forge, neoforge
├── build.gradle                 # агрегатор, общие репозитории
├── gradle.properties
├── gradle/wrapper/
├── common/                      # общий loader-нейтральный код (НЕ Gradle subproject)
│   └── src/main/
│       ├── java/ru/withoutyo/
│       │   ├── RussianTextNormalizer.java    # утилита «ё»→«е»
│       │   ├── WithoutYoConstants.java       # modid, display name, author
│       │   └── mixin/
│       │       ├── OptionalJeiMixinPlugin.java   # IMixinConfigPlugin
│       │       ├── vanilla/
│       │       │   ├── VanillaSearchMixin.java       # нормализация запроса
│       │       │   └── CreativeModeTabMixin.java     # нормализация индекса
│       │       └── jei/
│       │           └── JeiSearchMixin.java           # Mixin в GeneralizedSuffixTree
│       └── resources/
├── fabric/
│   ├── build.gradle             # fabric-loom
│   ├── gradle.properties
│   └── src/main/
│       ├── java/ru/withoutyo/fabric/WithoutYoFabric.java
│       └── resources/
│           ├── fabric.mod.json
│           ├── withoutyo.mixins.json
│           └── withoutyo.jei.mixins.json
├── forge/
│   ├── build.gradle             # ForgeGradle 5/6 (Mojang mappings)
│   ├── gradle.properties
│   └── src/main/
│       ├── java/ru/withoutyo/forge/WithoutYoForge.java
│       └── resources/
│           ├── META-INF/mods.toml
│           ├── META-INF/accesstransformer.cfg
│           ├── pack.mcmeta
│           ├── withoutyo.mixins.json
│           └── withoutyo.jei.mixins.json
└── neoforge/
    ├── build.gradle             # NeoGradle
    ├── gradle.properties
    └── src/main/
        ├── java/ru/withoutyo/neoforge/WithoutYoNeo.java
        └── resources/
            ├── META-INF/neoforged.mods.toml
            ├── pack.mcmeta
            ├── withoutyo.mixins.json
            └── withoutyo.jei.mixins.json
```

`common/` — **не** Gradle subproject, это просто директория с исходниками. Каждый
loader-проект подключает её через `sourceSets.main.java.srcDirs += '../common/src/main/java'`.
Это позволяет одному и тому же Java-коду (RussianTextNormalizer, JEI Mixin'ы) компилироваться
в разных контекстах маппингов (Yarn для Fabric, Mojang для Forge/NeoForge).

---

## Как собрать

### Подготовка окружения

1. Java JDK 17 (для Fabric 1.20.1 и Forge 1.20.1) **или** Java 21 (для NeoForge 1.21.x).
2. `git clone` этого репозитория.
3. Опционально: `gradle wrapper` (Gradle 8.7+) — иначе используйте установленный Gradle.

### Сборка конкретного subproject

```bash
# Fabric 1.20.1 (версия указана в fabric/gradle.properties)
./gradlew :fabric:assemble

# Forge 1.20.1
./gradlew :forge:jar

# NeoForge 1.21.x
./gradlew :neoforge:jar
```

Готовые jar'ы появятся в:
- `fabric/build/libs/withoutyo-1.0.0-1.20.1-fabric.jar`
- `forge/build/libs/withoutyo-1.0.0-1.20.1-forge.jar`
- `neoforge/build/libs/withoutyo-1.0.0-1.21-neoforge.jar`

---

## Как добавить новую версию

### Новая Fabric-версия

1. Скопируйте `fabric/` в новую директорию, например `fabric-1.21/`.
2. Измените `settings.gradle`: добавьте `include 'fabric-1.21'`.
3. Измените `fabric-1.21/gradle.properties` под нужную MC-версию.

Альтернативно, **внутри `fabric/`**: переключайте `minecraft_version` в `gradle.properties`
между сборками и запускайте `assemble` — fabric/ собирает для одной выбранной версии.

### Добавить Forge под 1.21.x

Forge и NeoForge — разные загрузчики. Для Forge 1.21.x (если он вам нужен отдельно от NeoForge)
скопируйте `forge/`, переключите `gradle.properties` на целевую MC + Forge версию.

### Поднять JEI на новую MC

Поменяйте `jei_version` в соответствующем subproject'е `gradle.properties`. JEI публикует
свои артефакты по схеме `mezz.jei:jei-<mc>-<platform>-api:<jei_version>` через
`https://maven.shedaniel.me/`.

---

## Разработка

### Как работает мод

1. **Vanilla creative search**: Mixin `VanillaSearchMixin` (на `CreativeModeInventoryScreen`)
   нормализует запрос, Mixin `CreativeModeTabMixin` (на `CreativeModeTab.buildContents`)
   нормализует имя предмета при индексировании.
2. **JEI search**: Mixin `JeiSearchMixin` подменяет аргументы `put(...)` и `getSearchResults(...)`
   в `mezz.jei.core.search.suffixtree.GeneralizedSuffixTree`.
3. **`OptionalJeiMixinPlugin`**: `IMixinConfigPlugin`, который отключает JEI Mixin'ы
   через `Class.forName("mezz.jei.core.search.suffixtree.GeneralizedSuffixTree")`,
   если JEI нет на classpath.

### Локальное тестирование

1. Запустите сборку нужного loader'а.
2. Скопируйте jar из `build/libs/` в `<mc>/mods/`.
3. Запустите Minecraft 1.20.1 (Forge) или 1.21.x (Fabric / NeoForge).
4. В игре откройте creative inventory → шерстяной таб → наберите «черная» (без ё).
   В списке должна появиться «Чёрная шерсть».
5. Установите JEI → повторите в окне поиска JEI.

### Что проверить при изменениях в MC-версиях

Yarn и Mojang имена используемых классов должны совпадать в обеих маппинговых системах:
- `CreativeModeInventoryScreen` и его метод `refreshSearchResults()`
- `CreativeModeTab` и его метод `buildContents()`
- `net.minecraft.network.chat.Component#getString()`
- `net.minecraft.client.gui.components.EditBox#getValue()`

Если в новой MC сигнатура какого-то метода изменится — нужная правка в одном файле
(например, `common/.../vanilla/VanillaSearchMixin.java`) сразу подхватится всеми loader'ами
через `srcDirs`.

---

## Лицензия

MIT — см. `LICENSE`.

Автор: **DanilaMint**
