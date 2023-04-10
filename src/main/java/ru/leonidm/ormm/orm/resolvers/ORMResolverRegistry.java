package ru.leonidm.ormm.orm.resolvers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.resolvers.builtin.ArrayResolver;
import ru.leonidm.ormm.orm.resolvers.builtin.EnumResolver;
import ru.leonidm.ormm.orm.resolvers.builtin.UUIDResolver;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ORMResolverRegistry {

    private static final Set<DatabaseResolver> ARGUMENT_RESOLVERS = new LinkedHashSet<>();

    static {
        ARGUMENT_RESOLVERS.add(new ArrayResolver());
        ARGUMENT_RESOLVERS.add(new UUIDResolver());
        ARGUMENT_RESOLVERS.add(new EnumResolver());
    }

    private ORMResolverRegistry() {

    }

    public static void addArgumentResolver(@NotNull DatabaseResolver databaseResolver) {
        ARGUMENT_RESOLVERS.add(databaseResolver);
    }

    @Nullable
    public static <T, F> F resolveFromDatabase(@NotNull ORMColumn<T, F> column, @NotNull Object object) throws CannotResolveException {
        for (DatabaseResolver databaseResolver : ARGUMENT_RESOLVERS) {
            try {
                if (databaseResolver.supportsFromType(column, object)) {
                    return (F) databaseResolver.resolveFromDatabase(column, object);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        throw new CannotResolveException();
    }

    @Nullable
    public static <T, F> F resolveToDatabase(@NotNull ORMColumn<T, F> column, @NotNull Object object) throws CannotResolveException {
        for (DatabaseResolver databaseResolver : ARGUMENT_RESOLVERS) {
            try {
                if (databaseResolver.supportsToType(column, object)) {
                    return (F) databaseResolver.resolveToDatabase(column, object);
                }
            } catch (CannotResolveException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        throw new CannotResolveException();
    }
}
