# ORMM

**ORMM** is a light-weight queries-builder with convenient usage, which is also easy to extend.

# Importing

* Maven:
```xml
<repositories>
  <repository>
    <id>smashup-repository</id>
    <name>SmashUp Repository</name>
    <url>https://mvn.smashup.ru/releases</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>ru.leonidm</groupId>
    <artifactId>ORMM</artifactId>
    <version>1.6.2</version>
  </dependency>
</dependencies>
```

* Gradle:
```groovy
repositories {
  maven { url 'https://mvn.smashup.ru/releases' }
}

dependencies {
  implementation 'ru.leonidm:ORMM:1.6.2'
}
```

# ORMM usage

**User** class as ORMM object:

```java
package ru.leonidm.ormm.tests;

import ...;

@Table("users")
public class User {

    @Column
    @PrimaryKey(autoIncrement = true)
    private int id;

    @Column(length = 100, unique = true)
    private String username;

    @Column
    private String description;

    @Column(name = "image", databaseClass = byte[].class)
    private BufferedImage profileImage;

    @Column(index = true, notNull = true)
    private int rating;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
```

**Comment** class as ORMM object with **@ForeignKey**:

```java
package ru.leonidm.ormm.tests;

import ...;

@Table("comments")
@CompositeIndex(value = {"id", "text"}, unique = false)
public class Comment {

    @Column
    @PrimaryKey(autoIncrement = true)
    private int id;

    @Column(notNull = true, databaseClass = int.class)
    @ForeignKey(table = "users", key = "id")
    private User user;

    @Column(notNull = true, length = 512)
    private String text;

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", user=" + user +
                ", text='" + text + '\'' +
                '}';
    }
}
```

Also, you can extend ORMM object's class from another ORMM object's class, but don't forget to register it:

```java
package ru.leonidm.ormm.tests;

import ...;

@Table("deleted_users")
public class DeletedUser extends User {

    @Column(name = "timestamp")
    private long deletedTimestamp;

}
```

**Queries**:

```java
package ru.leonidm.ormm.tests;

import ...;

import static ru.leonidm.ormm.orm.clauses.Order.*;
import static ru.leonidm.ormm.orm.clauses.Where.*;

public class QueryExample {

    public static void main(String[] args) {
        ORMDatabase database = new ORMDatabase(ORMDriver.MYSQL, "//localhost:3306/ormm?serverTimezone=UTC",
                "ormm", "ORMM");

        // Tables' registration
        database.addTable(User.class);
        database.addTable(DeletedUser.class);
        database.addTable(Comment.class);

        // InsertQuery
        database.insertQuery(User.class)
                .ignore(true)
                .value("username", "LeonidM")
                // You can set BufferedImage as field and even raw bytes
                .value("image", ImageIO.read(new File("leonidm.png")))
                // .queue() will start ORMTask in another thread
                .queue();

        database.insertQuery(User.class)
                .ignore(true)
                .value("username", "MdinoeL")
                .value("rating", 100)
                .queue();

        // You can make complicated queries like this:
        Comment comment = database.insertQuery(Comment.class)
                .value("user", database.selectQuery(User.class)
                        .where(compare("username", "=", "LeonidM"))
                        .single()
                        .complete())
                .value("text", "Aboba")
                .complete();


        // Select query
        database.selectQuery(User.class)
                .order(desc("rating"))
                .limit(10)
                // Also, you can send Consumer<User> as argument
                .queue(System.out::println);

        database.selectQuery(User.class)
                .where(compare("username", "=", "LeonidM"))
                .columns("id")
                .single();

        User user = database.selectQuery(User.class)
                .where(and(compare("username", "=", "LeonidM"), isNotNull("rating")))
                // .single() will return not List<User>, but only User
                .single()
                // .complete() will start ORMTask in another thread and wait for it's end
                .complete();

        List<Object> rawValues = database.selectQuery(User.class)
                .where(compare("username", "=", "LeonidM"))
                // You can also select only one column, or two, or more
                .columns("id")
                .single()
                .complete();

        // You can get SQL's query string as well
        String selectQueryString = database.selectQuery(User.class)
                .where(compare("username", "=", "LeonidM"))
                .columns("id")
                .single()
                // Or .getSQLQuery()
                .toString();

        // One more example of complicated query:
        Comment comment = database.selectQuery(Comment.class)
                .where(compare("user", "=", database.selectQuery(User.class)
                        .where(compare("username", "=", "LeonidM"))
                        .columns("id")
                        .single()))
                .single()
                .complete();


        // Update query
        database.updateQuery(User.class, user)
                // You can set BufferedImage as field and even raw bytes in this updateQuery too
                .set("image", new byte[]{1, 2, 3, 4, 5, 6, 7})
                .where(compare("username", "=", "MdinoeL"))
                .queue();

        database.updateQuery(User.class, user)
                .set("image", ImageIO.read(new File("test2.png")))
                .where(or(isNull("username"),
                        and(like("username", "%NULL%"),
                                compare("rating", "<=", 0))))
                .queue();

        // Delete query
        database.deleteQuery(User.class)
                .where(compare("username", "=", "LeonidM"))
                .queue();
    }
}
```

## Custom resolvers of objects

```java
public final class UUIDResolver implements DatabaseResolver {

    /**
     * @return true if object from field can be converted to database format
     */
    @Override
    public boolean supportsToType(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) {
        Class<?> databaseClass = column.getDatabaseClass();
        return fieldObject.getClass() == UUID.class && (databaseClass == String.class || databaseClass == byte[].class);
    }

    @Override
    public Object resolveToDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) throws Exception {
        if (fieldObject instanceof UUID uuid) {
            Class<?> databaseClass = column.getDatabaseClass();
            if (databaseClass == String.class) {
                return uuid.toString();
            } else if (databaseClass == byte[].class) {
                long most = uuid.getMostSignificantBits();
                long least = uuid.getLeastSignificantBits();
                return ArrayConverter.toBytes(new long[]{most, least});
            } else {
                throw new CannotResolveException();
            }
        }

        throw new CannotResolveException();
    }

    /**
     * @return true if object from database can be converted to field instance
     */
    @Override
    public boolean supportsFromType(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) {
        Class<?> objectClass = databaseObject.getClass();
        return column.getFieldClass() == UUID.class && (objectClass == String.class || objectClass == byte[].class);
    }

    @Override
    public Object resolveFromDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) throws Exception {
        if (databaseObject instanceof String string) {
            return UUID.fromString(string);
        } else if (databaseObject instanceof byte[] bytes) {
            long[] longs = ArrayConverter.toLongs(bytes);
            return new UUID(longs[0], longs[1]);
        } else {
            throw new CannotResolveException();
        }
    }
}
```

After you created class, you need to register it like here:

```java
ORMResolverRegistry.addArgumentResolver(new UUIDResolver());
```

Built-in argument resolvers:
* Primitives and their wrappers, also arrays of them
* String
* UUID
* Enum

# TODO:
* Make all queries as abstract classes or interfaces *(dialects)* \[1\]
* Cache for all tables
* Disable column names like `index`, `integer`, etc.
* Default values of the columns
* Use setters, not `Field#set()` when it is possible
* Use joins while working with `@ForeignKey`
* Calculate on duplicate
* Queries:
    * DeleteIndexQuery
    * ModifyColumnQuery
    * DeleteQuery for instance

\[1\] When I was designing ORMM, I thought that ORMM would only support **MySQL** and **SQLite**, so it hasn't abstract
queries now.
