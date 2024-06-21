package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;

@Table(value = "enum_test", allowUnsafeOperations = true)
public class EnumTest {

    @Column(length = 128, index = true)
    SomeEnum someEnum;

    @Column(databaseClass = int.class)
    SomeEnum intSomeEnum;

    enum SomeEnum {
        A, B, C;
    }

    @Test
    public void mysqlEnums() {
        test(Databases.MYSQL);
    }

    @Test
    public void mysqlPoolEnums() {
        test(Databases.MYSQL_POOL);
    }

    @Test
    public void mysqlHikariEnums() {
        test(Databases.MYSQL_HIKARI);
    }

    @Test
    public void sqliteEnums() {
        test(Databases.SQLITE);
    }

    private void test(@NotNull ORMDatabase database) {
        database.addTable(EnumTest.class);

        database.deleteQuery(EnumTest.class).complete();

        someEnum = SomeEnum.A;
        intSomeEnum = SomeEnum.C;

        database.insertQuery(EnumTest.class, this).complete();

        EnumTest e = database.selectQuery(EnumTest.class).single().complete();
        assertNotNull(e);

        assertEquals(someEnum, e.someEnum);
        assertEquals(intSomeEnum, e.intSomeEnum);
    }
}
