package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.PrimaryKey;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;

import java.util.List;

@Table(value = "insert_duplicate_test", allowUnsafeOperations = true)
public class InsertDuplicateTest {

    @Column
    @PrimaryKey
    private int id;

    @Column(length = 100, unique = true)
    private String someString;

    @Column
    private int someField;

    @Test
    public void mysqlInsertDuplicateUpdate() {
        test(Databases.MYSQL);
    }

    @Test
    public void sqliteInsertDuplicateUpdate() {
        test(Databases.SQLITE);
    }

    private void test(@NotNull ORMDatabase database) {
        database.addTable(InsertDuplicateTest.class);

        database.deleteQuery(InsertDuplicateTest.class).complete();
        id = 10;
        someString = "UNIQUE";
        someField = 10;

        InsertDuplicateTest test = database.insertQuery(InsertDuplicateTest.class, this).onDuplicateUpdate(false).complete();
        assertSame(this, test);

        assertThrows(IllegalStateException.class, () -> {
            database.insertQuery(InsertDuplicateTest.class, this).onDuplicateUpdate(false).complete();
        });

        assertDoesNotThrow(() -> {
            database.insertQuery(InsertDuplicateTest.class, this).onDuplicateUpdate(true).complete();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            database.insertQuery(InsertDuplicateTest.class, this).onDuplicateUpdate(true).ignore(true).complete();
        });

        InsertDuplicateTest i = new InsertDuplicateTest();
        i.id = 10;
        i.someField = 15;
        database.insertQuery(InsertDuplicateTest.class, i).onDuplicateUpdate(true).complete();

        List<InsertDuplicateTest> is = database.selectQuery(InsertDuplicateTest.class).complete();
        assertNotNull(is);
        assertEquals(1, is.size());

        assertEquals(15, is.get(0).someField);
    }
}
