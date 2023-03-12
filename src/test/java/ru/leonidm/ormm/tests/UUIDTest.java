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

import java.util.UUID;

@Table(value = "uuid_test", allowUnsafeOperations = true)
public class UUIDTest {

    @Column(databaseClass = String.class)
    private UUID uuid1;

    @Column(databaseClass = byte[].class)
    private UUID uuid2;

    @Test
    public void mysqlUuid() {
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
    public void sqliteUuid() {
        ORMDatabase database = new ORMDatabase(ORMDriver.SQLITE, ORMSettings.builder()
                .setHost("test.db")
                .build());
        test(database);
    }

    private void test(@NotNull ORMDatabase database) {
        database.addTable(UUIDTest.class);

        uuid1 = UUID.randomUUID();
        uuid2 = UUID.randomUUID();

        database.insertQuery(UUIDTest.class)
                .value("uuid1", uuid1)
                .value("uuid2", uuid2)
                .complete();

        UUIDTest u = database.selectQuery(UUIDTest.class).single().complete();
        assertNotNull(u);

        database.deleteQuery(UUIDTest.class).queue();

        assertEquals(u.uuid1, uuid1);
        assertEquals(u.uuid2, uuid2);
    }
}
