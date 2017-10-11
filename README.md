## Установка 
1. Добавить в файл application.properties логин и пароль к локальной базе,
2. Добавить в файл ConnectionServiceImpl.java переменные для доступа к требуемой базе,
3. Если хочется получать уведомления в скайп о процессе выполнения, нужно добавить в файл SkypeUtils.java необходимые переменные, которые можно использовать в методе MigrationServiceImpl::validatePermissionAndStartMigration

Есть 3 типа слушателей: 
- TaskListenerComplete
- TaskListenerPartComplete
- TaskListenerError

## Перед запуском 
Нужно выбрать один из подходов: автоматическое чтение из папки todo по версии спринта или скрипты из файла Queries.java.
Для первого варианта необходимо MigrationServiceImpl.DELTA присвоить значение, равное текущему устанавливаемому спринту. Программа вычитает все файлы из папки mvd-soop/soop-db/sql/todo, название которых начинается с "V" + sprint, заканчивается на "__MIGRATION.sql".
### MigrationServiceImpl.java
```
private final static int SPRINT = 67;

ReadingUtils.getQueriesInfoBySprint(SPRINT)
  .forEach(queryInfo -> queryPartialUpdate(queryInfo.get(0), queryInfo.get(1), queryInfo.get(2), connection));
```


Для второго варианта необходимо добавить запрос в файл Queries.java и использовать его в MigrationServiceImpl::startMigration. 
#### Queries.java
```
public static String q1 = ""
  + "UPDATE schema.my_table t SET some_field = 57\n"
	+ "WHERE t.my_table_id BETWEEN ? AND ?;";
```
#### MigrationServiceImpl.java
```
queryPartialUpdate(Queries.q1, "schema.my_table", "my_table_id", connection);
```
## Запуск программы
Запуск осуществляется методом main() из класса DemoSpringApplication
## Запуск миграции
1. Генерация ключей
```
http://localhost:8181/key/new/5
```
2. Запуск миграции
```
http://localhost:8181/migration/{key}/start
```
Вместо {key} использовать один из ключей. Потом он станет неактивным.

Шаблон для Postman можно взять [отсюда](https://www.getpostman.com/collections/8a14ac4f44bd2ce3aace).
