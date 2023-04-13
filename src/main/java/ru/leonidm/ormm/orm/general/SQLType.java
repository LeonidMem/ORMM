package ru.leonidm.ormm.orm.general;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public enum SQLType {

    INTEGER,
    BIGINT,
    FLOAT,
    DOUBLE,

    BLOB(SQLTypeMeta.builder().length(true).defaultLength(65535).indexable(driver -> switch (driver) {
        case MYSQL -> false;
        case SQLITE -> true;
    }).build()),

    VARCHAR(SQLTypeMeta.builder().length(true).defaultLength(-1).build()),
    UUID_VARCHAR(SQLTypeMeta.builder().length(true).defaultLength(36).build()) {
        @Override
        public String toString() {
            return "VARCHAR";
        }
    },

    TEXT(SQLTypeMeta.builder().length(true).defaultLength(65535).indexable(driver -> switch (driver) {
        case MYSQL -> false;
        case SQLITE -> true;
    }).build());

    private static final EnumMap<ORMDriver, Map<Class<?>, SQLType>> TYPES_BY_CLASS = new EnumMap<>(ORMDriver.class) {{
        put(ORMDriver.MYSQL, new HashMap<>() {{
            put(boolean.class, INTEGER);
            put(Boolean.class, INTEGER);
            put(byte.class, INTEGER);
            put(Byte.class, INTEGER);
            put(char.class, INTEGER);
            put(Character.class, INTEGER);
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
            put(char[].class, BLOB);
            put(Character[].class, BLOB);
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

            put(UUID.class, UUID_VARCHAR);
        }});

        put(ORMDriver.SQLITE, new HashMap<>() {{
            put(boolean.class, INTEGER);
            put(Boolean.class, INTEGER);
            put(byte.class, INTEGER);
            put(Byte.class, INTEGER);
            put(char.class, INTEGER);
            put(Character.class, INTEGER);
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
            put(char[].class, BLOB);
            put(Character[].class, BLOB);
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

            put(UUID.class, UUID_VARCHAR);
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
        return meta.length;
    }

    public int getDefaultLength() {
        return meta.defaultLength;
    }

    public boolean isIndexable(@NotNull ORMDriver driver) {
        return meta.indexablePredicate.test(driver);
    }

    @Nullable
    public static SQLType of(@NotNull ORMColumn<?, ?> column) {
        if (column.getDatabaseClass() == String.class) {
            if (column.getFieldClass() == UUID.class) {
                return UUID_VARCHAR;
            }

            int length = column.getMeta().length();

            if (length > 0 && length < 65536) {
                return VARCHAR;
            } else {
                return TEXT;
            }
        }

        return TYPES_BY_CLASS.get(column.getTable().getDatabase().getDriver()).get(column.getDatabaseClass());
    }

    @Nullable
    public static SQLType of(@NotNull String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
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

            private Builder() {
            }

            private Builder length(boolean length) {
                this.length = length;
                return this;
            }

            private Builder defaultLength(int defaultLength) {
                this.defaultLength = defaultLength;
                return this;
            }

            private Builder indexable(@NotNull Predicate<ORMDriver> indexablePredicate) {
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
