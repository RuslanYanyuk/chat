package com.test.utils.yaml;

import java.util.*;

/**
 * Represents table described in data set. Stores information such as
 * table's name, list of columns and {@link Row}s.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
public class Table {
    private final String tableName;

    private final Set<String> columns = new HashSet<String>();

    private final List<Row> rows = new ArrayList<Row>();

    public Table(String tableName) {
        this.tableName = tableName;
    }

    public void addRows(Collection<Row> rows) {
        this.rows.addAll(rows);
    }

    public void addColumns(Collection<String> columns) {
        this.columns.addAll(columns);
    }

    public String getTableName() {
        return tableName;
    }

    public Set<String> getColumns() {
        return Collections.unmodifiableSet(columns);
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }
}