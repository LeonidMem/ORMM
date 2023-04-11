package ru.leonidm.ormm.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.ORMDatabase;

@Table(value = "array_test", allowUnsafeOperations = true)
public class ArraysTest {

    public @Column boolean[] booleans1;
    public @Column Boolean[] booleans2;

    public @Column byte[] bytes1;
    public @Column Byte[] bytes2;

    public @Column short[] shorts1;
    public @Column Short[] shorts2;

    public @Column int[] ints1;
    public @Column Integer[] ints2;

    public @Column long[] longs1;
    public @Column Long[] longs2;

    public @Column float[] floats1;
    public @Column Float[] floats2;

    public @Column double[] doubles1;
    public @Column Double[] doubles2;

    public @Column char[] chars1;
    public @Column Character[] chars2;

    @Test
    public void mysqlArrays() {
        test(Databases.MYSQL);
    }

    @Test
    public void sqliteArrays() {
        test(Databases.SQLITE);
    }

    private void test(@NotNull ORMDatabase database) {
        database.addTable(ArraysTest.class);

        boolean[] booleans1 = {true, false};
        Boolean[] booleans2 = {false, true};

        byte[] bytes1 = {1, 2, 6, 29, 34, Byte.MAX_VALUE, Byte.MIN_VALUE};
        Byte[] bytes2 = {3, 6, 2, 7, 8, 9, Byte.MAX_VALUE, Byte.MIN_VALUE};

        short[] shorts1 = {1, 3, 9, 347, 23, Short.MAX_VALUE, Short.MIN_VALUE};
        Short[] shorts2 = {1, 4, 3, 4, 6, Short.MAX_VALUE, Short.MIN_VALUE};

        int[] ints1 = {237, 23279, 1273, 2323, Integer.MAX_VALUE, Integer.MIN_VALUE};
        Integer[] ints2 = {1, 23, 379, 293, 34, Integer.MAX_VALUE, Integer.MIN_VALUE};

        long[] longs1 = {123, 2357, 12326789, 34839403, Long.MAX_VALUE, Long.MIN_VALUE};
        Long[] longs2 = {1L, 23L, 2789L, 2618L, 347L, Long.MAX_VALUE, Long.MIN_VALUE};

        float[] floats1 = {1.123F, 34.97F, 289023.435F, 374839.212F, 2516.3479F, Float.MAX_VALUE, Float.MIN_VALUE};
        Float[] floats2 = {1.2719F, 239.3479F, 4568.12678F, 125367.34F, Float.MAX_VALUE, Float.MIN_VALUE};

        double[] doubles1 = {178.2379, 2378.23682, 1278.3468, 2378.332, Double.MAX_VALUE, Double.MIN_VALUE};
        Double[] doubles2 = {2681.347, 123678920.378, 341237.1, Double.MAX_VALUE, Double.MIN_VALUE};

        char[] chars1 = {'a', 'v', '1', '5', '6', '9', Character.MAX_VALUE, Character.MIN_VALUE};
        Character[] chars2 = {'i', 'o', 'p', Character.MAX_VALUE, Character.MIN_VALUE};

        database.insertQuery(ArraysTest.class)
                .value("booleans1", booleans1)
                .value("booleans2", booleans2)
                .value("bytes1", bytes1)
                .value("bytes2", bytes2)
                .value("shorts1", shorts1)
                .value("shorts2", shorts2)
                .value("ints1", ints1)
                .value("ints2", ints2)
                .value("longs1", longs1)
                .value("longs2", longs2)
                .value("floats1", floats1)
                .value("floats2", floats2)
                .value("doubles1", doubles1)
                .value("doubles2", doubles2)
                .value("chars1", chars1)
                .value("chars2", chars2)
                .complete();

        ArraysTest a = database.selectQuery(ArraysTest.class).single().complete();
        assertNotNull(a);

        database.deleteQuery(ArraysTest.class).queue();

        assertArrayEquals(a.booleans1, booleans1);
        assertArrayEquals(a.booleans2, booleans2);
        assertArrayEquals(a.bytes1, bytes1);
        assertArrayEquals(a.bytes2, bytes2);
        assertArrayEquals(a.shorts1, shorts1);
        assertArrayEquals(a.shorts2, shorts2);
        assertArrayEquals(a.ints1, ints1);
        assertArrayEquals(a.ints2, ints2);
        assertArrayEquals(a.longs1, longs1);
        assertArrayEquals(a.longs2, longs2);
        assertArrayEquals(a.floats1, floats1);
        assertArrayEquals(a.floats2, floats2);
        assertArrayEquals(a.doubles1, doubles1);
        assertArrayEquals(a.doubles2, doubles2);
        assertArrayEquals(a.chars1, chars1);
        assertArrayEquals(a.chars2, chars2);
    }
}
