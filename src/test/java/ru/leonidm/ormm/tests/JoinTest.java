package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;
import ru.leonidm.ormm.orm.clauses.JoinWhere;


@Table(value = "join_test", allowUnsafeOperations = true)
public class JoinTest {

    @Column
    private int id;

    @Column
    private int anotherId;

    @Test
    public void mysqlInner() {
        innerTest(Databases.MYSQL);
    }

    @Test
    public void sqliteInner() {
        innerTest(Databases.SQLITE);
    }

    private void innerTest(@NotNull ORMDatabase database) {
        database.addTable(JoinTest.class);
        database.addTable(AnotherTable.class);

        database.deleteQuery(JoinTest.class).complete();
        database.deleteQuery(AnotherTable.class).complete();

        database.insertQuery(AnotherTable.class)
                .value("id", 1)
                .value("name", "LeonidM")
                .complete();
        database.insertQuery(AnotherTable.class)
                .value("id", 2)
                .value("name", "MdinoeL")
                .complete();

        id = 4;
        anotherId = 1;

        database.insertQuery(JoinTest.class, this).complete();

        database.insertQuery(JoinTest.class)
                .value("id", 3)
                .value("another_id", 2)
                .complete();

        var list = database.selectQuery(JoinTest.class)
                .innerJoin(AnotherTable.class)
                .on(JoinWhere.compare("another_id", "=", "id"))
                .select("id", (joinTest, rawId) -> {
                    assertEquals(joinTest.anotherId, rawId);
                })
                .select("name", (joinTest, name) -> {
                    String toEqual = switch (joinTest.anotherId) {
                        case 1 -> "LeonidM";
                        case 2 -> "MdinoeL";
                        default -> fail();
                    };

                    assertEquals(toEqual, name);
                })
                .finish()
                .complete();

        assertNotNull(list);
        assertEquals(list.size(), 2);

        database.selectQuery(JoinTest.class)
                .single()
                .innerJoin(AnotherTable.class)
                .on(JoinWhere.compare("another_id", "=", "id"))
                .select("id", (joinTest, rawId) -> {
                    assertEquals(joinTest.anotherId, rawId);
                })
                .select("name", (joinTest, name) -> {
                    String toEqual = switch (joinTest.anotherId) {
                        case 1 -> "LeonidM";
                        case 2 -> "MdinoeL";
                        default -> fail();
                    };

                    assertEquals(toEqual, name);
                })
                .finish()
                .complete();
    }

    @Table(value = "join_test_2", allowUnsafeOperations = true)
    public static class AnotherTable {

        @Column
        private int id;

        @Column
        private String name;
    }
}
