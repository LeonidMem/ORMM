package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;

@Table(value = "insert_ignore_test", allowUnsafeOperations = true)
public class InsertIgnoreTest {

    @Column(unique = true)
    private int id;

    @Column
    private String a;

    @Test
    public void mysqlInsertIgnore() {
        test(Databases.MYSQL);
    }

    @Test
    public void mysqlPoolInsertIgnore() {
        test(Databases.MYSQL_POOL);
    }

    @Test
    public void mysqlHikariInsertIgnore() {
        test(Databases.MYSQL_HIKARI);
    }

    @Test
    public void sqliteInsertIgnore() {
        test(Databases.SQLITE);
    }

    private void test(@NotNull ORMDatabase database) {
        database.addTable(InsertIgnoreTest.class);

        database.deleteQuery(InsertIgnoreTest.class).complete();

        InsertIgnoreTest i1 = database.insertQuery(InsertIgnoreTest.class)
                .value("id", 1)
                .ignore(true)
                .complete();

        assertNotNull(i1);

        InsertIgnoreTest i2 = database.insertQuery(InsertIgnoreTest.class)
                .value("id", 1)
                .ignore(true)
                .complete();

        assertNull(i2);

        InsertIgnoreTest i3 = database.insertQuery(InsertIgnoreTest.class)
                .value("id", 1)
                .onDuplicateUpdate(true)
                .complete();

        assertNotNull(i3);
    }
}
