package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PoolTest {

    @Test
    public void mysqlPool() throws InterruptedException {
        test(Databases.MYSQL_POOL);
    }

    private void test(@NotNull ORMDatabase database) throws InterruptedException {
        database.addTable(PoolEntity.class);

        database.deleteQuery(PoolEntity.class).complete();

        List<Thread> threads = new ArrayList<>();
        Set<PoolEntity> set = new HashSet<>();

        for (int i = 0; i < 64; i++) {
            int finalI = i;

            Thread thread = new Thread(() -> {
                database.insertQuery(PoolEntity.class)
                        .value("value", finalI)
                        .complete();
            });

            threads.add(thread);

            set.add(new PoolEntity(i));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        List<PoolEntity> list = database.selectQuery(PoolEntity.class)
                .complete();
        assertNotNull(list);

        assertEquals(set, new HashSet<>(list));
    }

    @Table(value = "pool_entity", allowUnsafeOperations = true)
    public static class PoolEntity {

        @Column
        private int value;

        public PoolEntity() {

        }

        public PoolEntity(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PoolEntity that = (PoolEntity) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "PoolEntity{" +
                    "value=" + value +
                    '}';
        }
    }
}
