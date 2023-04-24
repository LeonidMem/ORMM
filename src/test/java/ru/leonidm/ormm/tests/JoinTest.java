package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.PrimaryKey;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;
import ru.leonidm.ormm.orm.clauses.JoinWhere;

import java.util.List;


@Table(value = "join_test", allowUnsafeOperations = true)
public class JoinTest {

    @Column
    @PrimaryKey
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
        database.addTable(AnotherTable2.class);

        database.deleteQuery(JoinTest.class).complete();
        database.deleteQuery(AnotherTable.class).complete();
        database.deleteQuery(AnotherTable2.class).complete();

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
                .selectOne("id", (joinTest, rawId) -> {
                    assertEquals(joinTest.anotherId, rawId);
                })
                .selectOne("name", (joinTest, name) -> {
                    String toEqual = switch (joinTest.anotherId) {
                        case 1 -> "LeonidM";
                        case 2 -> "MdinoeL";
                        default -> fail();
                    };

                    assertEquals(toEqual, name);
                })
                .closeJoin()
                .complete();

        assertNotNull(list);
        assertEquals(list.size(), 2);

        database.selectQuery(JoinTest.class)
                .single()
                .innerJoin(AnotherTable.class)
                .on(JoinWhere.compare("another_id", "=", "id"))
                .selectOne("id", (joinTest, rawId) -> {
                    assertEquals(joinTest.anotherId, rawId);
                })
                .selectOne("name", (joinTest, name) -> {
                    String toEqual = switch (joinTest.anotherId) {
                        case 1 -> "LeonidM";
                        case 2 -> "MdinoeL";
                        default -> fail();
                    };

                    assertEquals(toEqual, name);
                })
                .closeJoin()
                .complete();

        for (int i = 0; i < 5; i++) {
            database.insertQuery(AnotherTable2.class)
                    .value("id", i + 1)
                    .value("name", "LeonidM")
                    .complete();
        }

        for (int i = 0; i < 2; i++) {
            database.insertQuery(AnotherTable2.class)
                    .value("id", i + 6)
                    .value("name", "MdinoeL")
                    .complete();
        }

        var a = database.selectQuery(AnotherTable.class)
                .innerJoin(AnotherTable2.class)
                .on(JoinWhere.compare("name", "=", "name"))
                .selectMany("id", (anotherTable, objects) -> {
                    if (anotherTable.id == 1) {
                        assertEquals(List.of(1, 2, 3, 4, 5), objects);
                    } else if (anotherTable.id == 2) {
                        assertEquals(List.of(6, 7), objects);
                    } else {
                        fail();
                    }
                })
                .closeJoin()
                .complete();
        assertNotNull(a);
        assertEquals(2, a.size());

        var b = database.selectQuery(JoinTest.class)
                .innerJoin(AnotherTable.class)
                .on(JoinWhere.compare("another_id", "=", "id"))
                .closeJoin()
                .innerJoin(AnotherTable.class, AnotherTable2.class)
                .on(JoinWhere.compare("name", "=", "name"))
                .selectMany("id", (joinTest, objects) -> {
                    if (joinTest.anotherId == 1) {
                        assertEquals(List.of(1, 2, 3, 4, 5), objects);
                    } else if (joinTest.anotherId == 2) {
                        assertEquals(List.of(6, 7), objects);
                    } else {
                        fail();
                    }
                })
                .closeJoin()
                .complete();
        assertNotNull(b);
        assertEquals(2, b.size());

        var c = database.selectQuery(JoinTest.class)
                .single()
                .innerJoin(AnotherTable.class)
                .on(JoinWhere.compare("another_id", "=", "id"))
                .closeJoin()
                .innerJoin(AnotherTable.class, AnotherTable2.class)
                .on(JoinWhere.compare("name", "=", "name"))
                .selectMany("id", (joinTest, objects) -> {
                    if (joinTest.anotherId == 1) {
                        assertEquals(List.of(1, 2, 3, 4, 5), objects);
                    } else if (joinTest.anotherId == 2) {
                        assertEquals(List.of(6, 7), objects);
                    } else {
                        fail();
                    }
                })
                .closeJoin()
                .complete();
        assertNotNull(c);

        var d = database.selectQuery(JoinTest.class)
                .innerJoin(AnotherTable.class)
                .on(JoinWhere.compare("another_id", "=", "id"))
                .closeJoin()
                .innerJoin(AnotherTable.class, AnotherTable2.class)
                .on(JoinWhere.compare("name", "=", "name"))
                .selectMany("id", (joinTest, objects) -> {
                    if (joinTest.anotherId == 1) {
                        assertEquals(List.of(1, 2, 3, 4, 5), objects);
                    } else if (joinTest.anotherId == 2) {
                        assertEquals(List.of(6, 7), objects);
                    } else {
                        fail();
                    }
                })
                .closeJoin()
                .limit(2)
                .complete();
        assertNotNull(d);
        assertEquals(2, d.size());
    }

    @Table(value = "join_test_2", allowUnsafeOperations = true)
    public static class AnotherTable {

        @Column
        @PrimaryKey
        private int id;

        @Column
        private String name;
    }

    @Table(value = "join_test_3", allowUnsafeOperations = true)
    public static class AnotherTable2 {

        @Column
        private int id;

        @Column
        private String name;
    }
}
