package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.PrimaryKey;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;

@Table(value = "simple_test", allowUnsafeOperations = true)
public class SimpleTest {

    @Column
    @PrimaryKey
    private int a;
    @Column
    private long b;
    @Column
    private float c;
    @Column
    private double d;
    @Column
    private byte e;
    @Column
    private char f;

    @Test
    public void mysqlSimple() {
        test(Databases.MYSQL);
    }

    @Test
    public void sqliteSimple() {
        test(Databases.SQLITE);
    }

    private void test(@NotNull ORMDatabase database) {
        database.addTable(SimpleTest.class);

        a = 10;
        b = Long.MAX_VALUE - Integer.MAX_VALUE - 1;
        c = 1.11111f;
        d = 1.346d;
        e = 12;
        f = 127;

        SimpleTest s = database.insertQuery(SimpleTest.class, this).complete();
        assertSame(s, this);

        s = database.selectQuery(SimpleTest.class).single().complete();
        assertNotSame(s, this);
        assertNotNull(s);

        assertEquals(a, s.a);
        assertEquals(b, s.b);
        assertEquals(c, s.c);
        assertEquals(d, s.d);
        assertEquals(e, s.e);
        assertEquals(f, s.f);

        a = 15;
        b = 23892L;
        c = 0f;
        d = 1d;
        e = 78;
        f = 13;

        s = database.updateQuery(SimpleTest.class, this).complete();
        assertSame(s, this);

        assertEquals(a, s.a);
        assertEquals(b, s.b);
        assertEquals(c, s.c);
        assertEquals(d, s.d);
        assertEquals(e, s.e);
        assertEquals(f, s.f);

        database.deleteQuery(SimpleTest.class).queue();

    }
}
