package ru.leonidm.ormm.orm.clauses;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.QueryUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class JoinWhere {

    private static final Set<String> OPERANDS = new HashSet<>(Arrays.asList("<", "<=", "=", ">=", ">", "<>", "!="));

    @NotNull
    public static JoinWhere compare(@NotNull String column, @NotNull String operand, @NotNull String joinedColumn) {
        if (!OPERANDS.contains(operand)) {
            throw new IllegalArgumentException("Unknown operand \"" + operand + "\"");
        }

        return new JoinWhere(column, operand, joinedColumn);
    }

    private final String column;
    private final String operand;
    private final String joinedColumn;

    private JoinWhere(@NotNull String column, @NotNull String operand, @NotNull String joinedColumn) {
        this.column = column;
        this.operand = operand;
        this.joinedColumn = joinedColumn;
    }

    @NotNull
    public String build(@NotNull ORMTable<?> table, @NotNull ORMTable<?> joinedTable) {
        if (!table.getColumnsNames().contains(column)) {
            throw new IllegalArgumentException("Cannot find column \"%s\" in table \"%s\"".formatted(column, table.getName()));
        }

        if (!joinedTable.getColumnsNames().contains(joinedColumn)) {
            throw new IllegalArgumentException("Cannot find column \"%s\" in table \"%s\"".formatted(joinedColumn, joinedTable.getName()));
        }

        return QueryUtils.getTableName(table) + '.' + column + ' ' + operand + ' ' + QueryUtils.getTableName(joinedTable) + '.' + joinedColumn;
    }
}
