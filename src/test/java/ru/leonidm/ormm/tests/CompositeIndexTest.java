package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.CompositeIndex;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;

@Table("composite_index_test")
@CompositeIndex(value = {"a", "b"}, unique = true)
@CompositeIndex(value = {"a", "c"})
public class CompositeIndexTest {

    @Column
    private int a;

    @Column
    private int b;

    @Column
    private int c;

    @Test
    public void mysqlCompositeIndexes() {
        test(Databases.MYSQL);
    }

    @Test
    public void mysqlPoolCompositeIndexes() {
        test(Databases.MYSQL_POOL);
    }

    @Test
    public void mysqlHikariCompositeIndexes() {
        test(Databases.MYSQL_HIKARI);
    }

    @Test
    public void sqliteCompositeIndexes() {
        test(Databases.SQLITE);
    }

    public void test(@NotNull ORMDatabase database) {
        database.addTable(CompositeIndexTest.class);

        a = 1;
        b = 1;
        c = 1;

        database.insertQuery(CompositeIndexTest.class, this)
                .complete();

        assertThrows(IllegalStateException.class, () -> {
            database.insertQuery(CompositeIndexTest.class, this)
                    .complete();
        });

        database.insertQuery(CompositeIndexTest.class, this)
                .ignore(true)
                .complete();
    }
}
