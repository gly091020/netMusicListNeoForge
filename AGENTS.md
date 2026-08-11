# Repository Guidelines

NeoForge 1.21.1 port of the *Net Music: Better Experience* mod (网络音乐机：更好的体验), built with Gradle and the NeoGradle `moddev` plugin and licensed under the PolyForm Shield License 1.0.0.

## 回答
- 回答统一用中文

## Project Structure & Module Organization

- `src/main/java/com/gly091020/netMusicListNeoforge/` — all mod source, organized by feature (`block`, `client`, `config`, `create`, `datagen`, `entity`, `hud`, `item`, `jade`, `mixin`, `packet`, `sounds`, `util`).
- `src/main/resources/` — hand-authored assets and data: translations (`assets/net_music_list/lang/`), textures, models, the Patchouli manual, and bundled MP3 files.
- `src/generated/resources/` — output of the data generators. Gitignored; regenerate it with `runData` rather than editing by hand.
- `src/main/templates/` — mod metadata template expanded by the `generateModMetadata` task.
- `libs/` — local jars used as flat-directory dependencies (e.g., `net_music_login_need-1.21.1-0.4.jar`).
- `run/` — local development game data; gitignored.

## Build, Test, and Development Commands

On Windows use `.\gradlew.bat`; elsewhere use `./gradlew`.

- `gradlew build` — compiles the mod and assembles the jar into `build/libs/`.
- `gradlew runClient` / `gradlew runServer` — launches the dev client/server for manual testing.
- `gradlew runData` — runs data generators, writing to `src/generated/resources/`.
- `gradlew runGameTestServer` — runs registered game tests (none are registered yet).
- `gradlew modrinth` — publishes a release; requires the `MODRINTH_TOKEN` environment variable.

Note: `gradle.properties` contains local proxy settings (`127.0.0.1:7897`); adjust or remove them if your network differs.

## Coding Style & Naming Conventions

- Use Java 21 with 4-space indentation and no tabs. No formatter or linter is configured; match the surrounding code.
- Follow PascalCase for classes, camelCase for methods and fields, and `UPPER_SNAKE_CASE` for constants.
- Keep feature packages under `com.gly091020.netMusicListNeoforge` and register through the central `NetMusicList` class.
- The mod id is lowercase snake-case (`net_music_list`); keep `gradle.properties` and the `@Mod` annotation in sync.
- 不需要太多的注释，仅在可能为api的部分使用代码块注释，其他地方用普通注释标明关键部分

## Testing Guidelines

- No test source set or registered game tests exist yet. Treat `datagen` as the de facto validation: item, block, or model changes should leave `runData` producing a clean diff.
- New unit tests go in `src/test/java` and run with `gradlew test`; game tests run via `runGameTestServer`.

## Commit & Pull Request Guidelines

- The Git history uses short, descriptive summaries, mostly in Chinese (e.g., `修复一处问题`, `更改版本号`), without a strict prefix convention. Keep each commit to one logical change and describe what changed and why.
- Work on the `master` branch. Remotes: `gitee` (gly091020) and `github` (NotEnoughNetMusic).
- Pull requests need a clear description of the change and its motivation, references to related issues, and evidence of manual verification via `runClient`/`runServer`. Keep diffs scoped.

## 参考代码

- `./IamMusicPlayer_FIX`是一个更加完善的音乐播放模组，如果用户提到重构时请参考那个项目的代码，注意此项目保持简介，所以不能引入`LavaPlayer`，主要参考的是那个项目的`Ringer`系统
