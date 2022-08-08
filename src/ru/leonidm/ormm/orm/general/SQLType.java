package ru.leonidm.ormm.orm.general;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public enum SQLType {

    // TODO: timestamp

    INTEGER, BIGINT, FLOAT, DOUBLE,

    BLOB(SQLTypeMeta.builder().length(true).defaultLength(65535).indexable(driver -> switch(driver) {
        case MYSQL -> false;
        case SQLITE -> true;
    }).build()),

    VARCHAR(SQLTypeMeta.builder().length(true).defaultLength(-1).build()),

    TEXT(SQLTypeMeta.builder().length(true).defaultLength(65535).indexable(driver -> switch(driver) {
        case MYSQL -> false;
        case SQLITE -> true;
    }).build());

    private static final EnumMap<ORMDriver, Map<Class<?>, SQLType>> typesByClass = new EnumMap<>(ORMDriver.class) {{
        put(ORMDriver.MYSQL, new HashMap<>() {{
            put(byte.class, INTEGER);
            put(Byte.class, INTEGER);
            put(short.class, INTEGER);
            put(Short.class, INTEGER);
            put(int.class, INTEGER);
            put(Integer.class, INTEGER);

            put(long.class, BIGINT);
            put(Long.class, BIGINT);

            put(float.class, FLOAT);
            put(Float.class, FLOAT);

            put(double.class, DOUBLE);
            put(Double.class, DOUBLE);

            put(boolean[].class, BLOB);
            put(Boolean[].class, BLOB);
            put(byte[].class, BLOB);
            put(Byte[].class, BLOB);
            put(short[].class, BLOB);
            put(Short[].class, BLOB);
            put(int[].class, BLOB);
            put(Integer[].class, BLOB);
            put(long[].class, BLOB);
            put(Long[].class, BLOB);
            put(float[].class, BLOB);
            put(Float[].class, BLOB);
            put(double[].class, BLOB);
            put(Double[].class, BLOB);
            put(char[].class, BLOB);
            put(Character[].class, BLOB);
        }});

        put(ORMDriver.SQLITE, new HashMap<>() {{
            put(byte.class, INTEGER);
            put(Byte.class, INTEGER);
            put(short.class, INTEGER);
            put(Short.class, INTEGER);
            put(int.class, INTEGER);
            put(Integer.class, INTEGER);
            put(long.class, INTEGER);
            put(Long.class, INTEGER);

            put(float.class, FLOAT);
            put(Float.class, FLOAT);

            put(double.class, DOUBLE);
            put(Double.class, DOUBLE);

            put(boolean[].class, BLOB);
            put(Boolean[].class, BLOB);
            put(byte[].class, BLOB);
            put(Byte[].class, BLOB);
            put(short[].class, BLOB);
            put(Short[].class, BLOB);
            put(int[].class, BLOB);
            put(Integer[].class, BLOB);
            put(long[].class, BLOB);
            put(Long[].class, BLOB);
            put(float[].class, BLOB);
            put(Float[].class, BLOB);
            put(double[].class, BLOB);
            put(Double[].class, BLOB);
            put(char[].class, BLOB);
            put(Character[].class, BLOB);
        }});
    }};

    private final SQLTypeMeta meta;

    SQLType(SQLTypeMeta meta) {
        this.meta = meta;
    }

    SQLType() {
        this(SQLTypeMeta.builder().build());
    }

    public boolean hasLength() {
        return this.meta.length;
    }

    public int getDefaultLength() {
        return this.meta.defaultLength;
    }

    public boolean isIndexable(@NotNull ORMDriver driver) {
        return this.meta.indexablePredicate.test(driver);
    }

    @Nullable
    public static SQLType of(@NotNull ORMColumn<?, ?> column) {
        if(column.getDatabaseClass() == String.class) {
            int length = column.getMeta().length();

            if(length > 0 && length < 65536) {
                return VARCHAR;
            }
            else {
                return TEXT;
            }
        }

        return typesByClass.get(column.getTable().getDatabase().getDriver()).get(column.getDatabaseClass());
    }

    @Nullable
    public static SQLType of(@NotNull String name) {
        try {
            return valueOf(name);
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    private static class SQLTypeMeta {

        private static Builder builder() {
            return new Builder();
        }

        private final boolean length;
        private final int defaultLength;
        private final Predicate<ORMDriver> indexablePredicate;

        private SQLTypeMeta(boolean length, int defaultLength, Predicate<ORMDriver> indexablePredicate) {
            this.length = length;
            this.defaultLength = defaultLength;
            this.indexablePredicate = indexablePredicate;
        }

        private static class Builder {

            private boolean length = false;
            private int defaultLength = -1;
            private Predicate<ORMDriver> indexablePredicate = driver -> true;

            private Builder() {}

            private Builder length(boolean length) {
                this.length = length;
                return this;
            }

            private Builder defaultLength(int defaultLength) {
                this.defaultLength = defaultLength;
                return this;
            }

            private Builder indexable(Predicate<ORMDriver> indexablePredicate) {
                this.indexablePredicate = indexablePredicate;
                return this;
            }

            private Builder indexable(boolean indexable) {
                this.indexablePredicate = driver -> indexable;
                return this;
            }

            private SQLTypeMeta build() {
                return new SQLTypeMeta(length, defaultLength, indexablePredicate);
            }
        }
    }
}
