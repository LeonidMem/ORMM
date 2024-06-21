package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.PrimaryKey;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;

@Table(value = "auto_increment_test", allowUnsafeOperations = true)
public class AutoIncrementTest {

    @Column
    @PrimaryKey(autoIncrement = true)
    private int id;

    @Test
    public void mysqlAutoIncrement() {
        test(Databases.MYSQL);
    }

    @Test
    public void mysqlPoolAutoIncrement() {
        test(Databases.MYSQL_POOL);
    }

    @Test
    public void sqliteAutoIncrement() {
        test(Databases.SQLITE);
    }

    private void test(@NotNull ORMDatabase database) {
        database.addTable(AutoIncrementTest.class);

        database.deleteQuery(AutoIncrementTest.class).complete();

        var a = database.insertQuery(AutoIncrementTest.class)
                .complete();
        assertNotNull(a);

        var b = database.insertQuery(AutoIncrementTest.class)
                .complete();
        assertNotNull(b);

        assertEquals(1, b.id - a.id);
    }
}
