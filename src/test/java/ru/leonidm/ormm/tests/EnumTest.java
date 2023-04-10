package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

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
        ORMDatabase database = new ORMDatabase(ORMDriver.MYSQL, ORMSettings.builder()
                .setHost("localhost")
                .setPort(3306)
                .setDatabaseName("ormm")
                .setUser("ormm")
                .setPassword("ormm")
                .build());
        test(database);
    }

    @Test
    public void sqliteEnums() {
        ORMDatabase database = new ORMDatabase(ORMDriver.SQLITE, ORMSettings.builder()
                .setHost("test.db")
                .build());
        test(database);
    }

    private void test(@NotNull ORMDatabase database) {
        database.addTable(EnumTest.class);

        someEnum = SomeEnum.A;
        intSomeEnum = SomeEnum.C;

        database.insertQuery(EnumTest.class, this).complete();

        EnumTest e = database.selectQuery(EnumTest.class).single().complete();
        assertNotNull(e);

        assertEquals(someEnum, e.someEnum);
        assertEquals(intSomeEnum, e.intSomeEnum);

        database.deleteQuery(EnumTest.class).queue();
    }
}
