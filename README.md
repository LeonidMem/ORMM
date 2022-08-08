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

    @Column(name = "image", databaseClass = byte[].class,
            loadFunction = "ru.leonidm.ormm.tests.ImageUtils.decode",
            saveFunction = "ru.leonidm.ormm.tests.ImageUtils.encode")
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

**ImageUtils** class:
```java
package ru.leonidm.ormm.tests;

import ...;

public class ImageUtils {

    private ImageUtils() {}

    @Nullable
    public static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static byte[] encode(BufferedImage image) {
        if(image == null) return null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }

        return outputStream.toByteArray();
    }
}

```

**Comment** class as ORMM object with **@ForeignKey**:
```java
package ru.leonidm.ormm.tests;

import ...;

@Table("comments")
public class Comment {

    @Column
    @PrimaryKey(autoIncrement = true)
    private int id;

    @Column(notNull = true, databaseClass = int.class)
    @ForeignKey(table = "users", key = "id")
    private User user;

    @Column(notNull = true)
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
                        .waitQueue())
                .value("text", "Aboba")
                .waitQueue();

        
        
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
                // .waitQueue() will start ORMTask in another thread and wait for it's end
                .waitQueue();

        List<Object> rawValues = database.selectQuery(User.class)
                .where(compare("username", "=", "LeonidM"))
                // You can also select only one column, or two, or more
                .columns("id")
                .single()
                .waitQueue();

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
                .waitQueue();
        
        
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
    }
}
```


# TODO:
* Make all queries as abstract classes or interfaces \[1\]
* Cache for all tables
* Tests
* Integer to Long cast
* InsertQuery with provided object
* Disable column names like `index`, `integer`, etc.
* Default values of the columns
* Queries:
  * DeleteIndexQuery
  * ModifyColumnQuery
  * DeleteQuery
* Throw an exception if table is going to be registered again
* Add support for enums as columns

\[1\] When I was designing ORMM, I thought that ORMM would only support **MySQL** and **SQLite**, so it hasn't abstract
queries now.
