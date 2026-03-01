# SFSExtensions

Серверный Java-проект с расширениями для SmartFoxServer 2X. Репозиторий содержит bootstrap-расширения зоны, лобби и игровое расширение комнаты, а также отдельный `roomModule` с логикой ролей и разрешений.

## Что есть в проекте

- авторизация пользователя через БД;
- регистрация и восстановление пароля по email;
- создание и обновление лобби;
- создание игровых комнат и управление участниками;
- серверная обработка движения игрока;
- хранение состояния комнаты и игроков;
- коллизии, raycast и rewind/snapshot-логика для сетевой игры.

## Структура

```text
src/com/a51integrated/sfs2x/
├── bootstrap/   # zone-level bootstrap extensions
├── extensions/  # room extensions: lobby и game
├── handlers/    # client/server handlers
├── services/    # бизнес-логика, БД, collision, room state
├── loop/        # периодические игровые циклы
├── models/      # модели домена
├── data/        # DTO, state, math, collision data
├── helpers/     # утилиты и константы команд
└── config/      # runtime-конфигурация

roomModule/src/koban/roomModule/
├── ERoomRole.java
├── RoleService.java
├── RoomAction.java
└── RoomPermissionManager.java
```

## Основные расширения

### Zone bootstrap

- `GameZoneBootstrapExtension`
  - логин пользователя;
  - смена привилегий;
  - создание лобби;
  - вход в лобби.
- `GuestZoneBootstrapExtension`
  - регистрация;
  - восстановление пароля.
- `GlobalMaintenanceExtension`
  - фоновая очистка истекших reset-токенов.

### Room extensions

- `LobbyExtension`
  - обновление данных лобби;
  - кик игроков;
  - старт игры;
  - создание игровой комнаты.
- `GameExtension`
  - обработка входа/выхода игроков;
  - приём клиентских input frame;
  - raycast;
  - запуск циклов движения и отладки коллизий с периодом `33 ms`.

## Ключевые команды SmartFoxServer

Имена команд собраны в `src/com/a51integrated/sfs2x/helpers/SFSResponseHelper.java`.

Основные из них:

- `createLobby`
- `userJoinRoom`
- `updateLobbyData`
- `createGameRoom`
- `roomStartGame`
- `kickUserInRoom`
- `playerPreconditionState`
- `playerServerState`
- `raycast`
- `registerResult`
- `restoreResult`

## Конфигурация

Runtime-настройки лежат в:

`src/com/a51integrated/sfs2x/config/config.properties`

Файл содержит:

- имена таблиц пользователей и reset-токенов;
- путь к collision map JSON;
- настройки email API;
- TTL и длину reset-токена;
- базовый URL для восстановления пароля.

### Важно

- в `config.properties` сейчас лежат чувствительные значения;
- перед публикацией или переносом проекта их стоит вынести в безопасное хранилище или локальные override-файлы;
- `collision.map.path` задан абсолютным путём, его нужно адаптировать под локальное окружение/сервер.

## Сборка

Готового `Maven`/`Gradle`-пайплайна в репозитории нет. Проект рассчитан на сборку из IDE.

### IntelliJ IDEA

В репозитории уже есть модульные файлы:

- `SFSExtensions.iml`
- `roomModule/RoomModule.iml`

Обычный рабочий процесс:

1. Открыть проект в IntelliJ IDEA.
2. Подключить SmartFoxServer библиотеки из `lib/jarlib/`.
3. Собрать JAR для основного extension-модуля.
4. При необходимости отдельно собрать `roomModule`.
5. Разместить собранные артефакты в директориях расширений SmartFoxServer.

## Зависимости

В `lib/jarlib/` уже лежат основные библиотеки:

- `sfs2x.jar`
- `sfs2x-core.jar`
- `jackson-*`
- `joml`
- `RoomModule.jar`

Также в `lib/` присутствуют vendored-файлы и локальная tree `apache-tomcat`. Их лучше считать внешними зависимостями и не редактировать без необходимости.

## Развёртывание

Точная схема деплоя зависит от конфигурации SmartFoxServer, но в целом процесс такой:

1. Собрать JAR расширения.
2. Убедиться, что `config.properties` соответствует окружению.
3. Настроить zone extensions на bootstrap-классы:
   - `com.a51integrated.sfs2x.bootstrap.GameZoneBootstrapExtension`
   - `com.a51integrated.sfs2x.bootstrap.GuestZoneBootstrapExtension`
   - `com.a51integrated.sfs2x.bootstrap.GlobalMaintenanceExtension`
4. Настроить room extensions:
   - `com.a51integrated.sfs2x.extensions.LobbyExtension`
   - `com.a51integrated.sfs2x.extensions.GameExtension`
5. Перезапустить extension или SmartFoxServer.

## Проверка вручную

Автотестов в проекте сейчас нет, поэтому основная проверка ручная:

1. Проверить регистрацию пользователя.
2. Проверить логин существующим пользователем.
3. Проверить восстановление пароля и запись reset-токена в БД.
4. Создать лобби и обновить его данные.
5. Создать игровую комнату из лобби.
6. Подключить нескольких игроков и проверить join/leave/disconnect сценарии.
7. Проверить синхронизацию движения, коллизии и `raycast`.

## Что стоит учитывать при доработке

- `out/` и `jaroutput/` выглядят как build-артефакты, их лучше не коммитить;
- при изменении `lib/` стоит явно фиксировать это в описании изменений;
- проект использует SmartFoxServer API напрямую, поэтому большая часть поведения зависит от конфигурации зоны, комнат и БД на стороне SFS.
