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

/**
 * The JdbcTemplate class provides a simple way to execute SQL queries and updates using JDBC.
 * It encapsulates common JDBC operations and handles exceptions by throwing a DataAccessException.
 */
public class JdbcTemplate {
    private final DataSource dataSource;
    private final ResultSetMapper resultSetMapper = new ResultSetMapper();

    /**
     * Constructs a new JdbcTemplate instance with the given DataSource.
     *
     * @param dataSource The DataSource to be used for database connections.
     */
    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Executes a SQL query and maps the result set to a list of objects using the provided RowMapper.
     *
     * @param sql       The SQL query to execute.
     * @param rowMapper The RowMapper to use for mapping each result row to a Java object.
     * @param <T>       The type of objects to be returned.
     * @return A list of Java objects resulting from the query execution.
     * @throws DataAccessException If there is an error during the database operation.
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            return resultSetMapper.mapResultSetToList(resultSet, rowMapper);

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /**
     * Executes a SQL query and maps the first row of the result set to an object using the provided RowMapper.
     *
     * @param sql       The SQL query to execute.
     * @param rowMapper The RowMapper to use for mapping the result row to a Java object.
     * @param params    The parameters to be bound to the query.
     * @param <T>       The type of the object to be returned.
     * @return The Java object resulting from the query execution, or null if no result was found.
     * @throws DataAccessException If there is an error during the database operation.
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        ValidationUtils.validateRowMapper(rowMapper);
        ValidationUtils.validateSql(sql);
        ValidationUtils.validateParamArray(params);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = executeQueryWithParameters(statement, params)) {

            if (resultSet.next()) {
                return rowMapper.map(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    /**
     * Executes a SQL update statement with the provided parameters.
     *
     * @param sql    The SQL update statement to execute.
     * @param params The parameters to be bound to the update statement.
     * @return The number of rows affected by the update.
     * @throws DataAccessException If there is an error during the database operation.
     */
    public int update(String sql, Object... params) {
        ValidationUtils.validateSql(sql);
        ValidationUtils.validateParamArray(params);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private ResultSet executeQueryWithParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement.executeQuery();
    }
}
