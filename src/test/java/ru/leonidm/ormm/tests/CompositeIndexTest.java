package ru.leonidm.ormm.tests;

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
    public void sqliteCompositeIndexes() {
        test(Databases.SQLITE);
    }

    public void test(@NotNull ORMDatabase database) {
        database.addTable(CompositeIndexTest.class);
    }
}
