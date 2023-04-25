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
import ru.leonidm.ormm.orm.clauses.Where;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    @Column
    private boolean g;
    @Column
    private boolean h;

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

        database.deleteQuery(SimpleTest.class).complete();

        a = 10;
        b = Long.MAX_VALUE - Integer.MAX_VALUE - 1;
        c = 1.11111f;
        d = 1.346d;
        e = 12;
        f = 127;
        g = true;
        h = false;

        SimpleTest s = database.insertQuery(SimpleTest.class, this).complete();
        assertSame(this, s);

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
        g = false;
        h = true;

        s = database.updateQuery(SimpleTest.class, this).complete();
        assertSame(this, s);

        assertEquals(a, s.a);
        assertEquals(b, s.b);
        assertEquals(c, s.c);
        assertEquals(d, s.d);
        assertEquals(e, s.e);
        assertEquals(f, s.f);

        database.addTable(Entity.class);
        database.deleteQuery(Entity.class).complete();

        List<Entity> entities = new ArrayList<>();
        List<List<Entity>> entitiesByMod = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entitiesByMod.add(new ArrayList<>());
        }

        for (int i = 0; i < 100; i++) {
            Entity entity = database.insertQuery(Entity.class)
                    .value("string", String.valueOf(i % 10))
                    .complete();

            entities.add(entity);
            entitiesByMod.get(i % 10).add(entity);
        }

        var queriedEntities = database.selectQuery(Entity.class).complete();
        assertNotNull(queriedEntities);
        assertEquals(entities, queriedEntities);

        for (int i = 0; i < 10; i++) {
            var queriedEntitiesByMod = database.selectQuery(Entity.class)
                    .where(Where.compare("string", "=", String.valueOf(i)))
                    .complete();

            assertNotNull(queriedEntitiesByMod);
            assertEquals(Set.copyOf(entitiesByMod.get(i)), Set.copyOf(queriedEntitiesByMod));
        }

        int minId = (int) database.selectQuery(Entity.class)
                .min("id")
                .complete();

        int maxId = (int) database.selectQuery(Entity.class)
                .max("id")
                .complete();

        assertEquals(99, maxId - minId);

        Long count = database.selectQuery(Entity.class)
                .count("id")
                .complete();

        assertEquals(100, count);
    }

    @Table(value = "simple_entities_test", allowUnsafeOperations = true)
    public static class Entity {

        @Column
        @PrimaryKey(autoIncrement = true)
        private int id;

        @Column
        private String string;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Entity entity = (Entity) o;
            return id == entity.id
                    && string.equals(entity.string);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, string);
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "id=" + id +
                    ", string='" + string + '\'' +
                    '}';
        }
    }
}
