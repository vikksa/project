package com.vikson.projects.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;

public class JDBCUtils {

    private JDBCUtils() {
    }

    public static Array createArray(NamedParameterJdbcTemplate template, String typeName, Object...values) throws SQLException {
        return createArray(template.getJdbcTemplate(), typeName, values);
    }

    public static Array createArray(JdbcTemplate template, String typeName, Object...values) throws SQLException  {
        try (Connection connection = template.getDataSource().getConnection()) {
            return connection.createArrayOf(typeName, values);
        }
    }

}
