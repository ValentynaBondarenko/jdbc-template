package com.bondarenko.template;

import com.bondarenko.exception.DataAccessException;
import com.bondarenko.mapper.ResultSetMapper;
import com.bondarenko.mapper.RowMapper;
import com.bondarenko.template.validation.ValidationUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class NamedParameterJdbcTemplate {
    private final DataSource dataSource;

    public NamedParameterJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Executes the given SQL query to create a prepared statement with a list of arguments to bind to the query,
     * and maps a single result row to a Java object using a provided RowMapper.
     *
     * @param sql       The SQL query to execute.
     * @param paramMap  The map of named parameters and their values.
     * @param rowMapper The RowMapper to use for mapping the result row to a Java object.
     * @return The Java object resulting from the query execution, or null if no result was found.
     */
    public <T> T queryForObject(String sql, Map<String, ?> paramMap, RowMapper<T> rowMapper) {
        ValidationUtils.validateRowMapper(rowMapper);
        ValidationUtils.validateSql(sql);
        ValidationUtils.validateParamMap(paramMap);

        for (Map.Entry<String, ?> entry : paramMap.entrySet()) {
            sql = sql.replace(":" + entry.getKey(), "?");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setParameters(paramMap, statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.map(resultSet);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /**
     * Issue an update via a prepared statement, binding the given arguments.
     *
     * @param sql      The SQL query to execute.
     * @param paramMap The map of named parameters and their values.
     * @return The number of rows affected by the update.
     */
    public int update(String sql, Map<String, Object> paramMap) {
        ValidationUtils.validateSql(sql);
        ValidationUtils.validateParamMap(paramMap);

        for (Map.Entry<String, ?> entry : paramMap.entrySet()) {
            sql = sql.replace(":" + entry.getKey(), "?");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int index = 1;
            for (Map.Entry<String, ?> entry : paramMap.entrySet()) {
                statement.setObject(index, entry.getValue());
                index++;
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /**
     * Query given SQL to create a prepared statement from SQL, mapping each row to a Java object via a RowMapper.
     *
     * @param sql       The SQL query to execute.
     * @param rowMapper The RowMapper to use for mapping each result row to a Java object.
     * @return The list of Java objects resulting from the query execution.
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        ValidationUtils.validateSql(sql);
        ValidationUtils.validateRowMapper(rowMapper);
        ResultSetMapper resultSetMapper = new ResultSetMapper();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();
            return resultSetMapper.mapResultSetToList(resultSet, rowMapper);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }


    private static void setParameters(Map<String, ?> paramMap, PreparedStatement statement) {
        int index = 1;
        for (Map.Entry<String, ?> entry : paramMap.entrySet()) {
            try {
                statement.setObject(index, entry.getValue());
            } catch (SQLException e) {
                throw new DataAccessException("Error setting parameters", e);
            }
            index++;
        }
    }
}
