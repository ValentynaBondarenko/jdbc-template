package com.bondarenko.template;

import com.bondarenko.mapper.ResultSetMapper;
import com.bondarenko.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedParameterJdbcTemplate<T> implements JdbcOperations {
    private final DataSource dataSource;
    private final ResultSetMapper<T> resultSetMapper;

    public NamedParameterJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.resultSetMapper = new ResultSetMapper<>();
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        if (rowMapper == null || sql == null || params.length % 2 != 0) {
            throw new IllegalArgumentException("RowMapper or SQL should not be null or params should be even");
        }
        for (int i = 0; i < params.length; i += 2) {
            if (!(params[i] instanceof String)) {
                throw new IllegalArgumentException("Even indexed params should be string representing the named parameter.");
            }
            sql = sql.replace(":" + params[i], "?");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i += 2) {
                statement.setObject(i / 2 + 1, params[i + 1]);
            }

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return rowMapper.map(resultSet);
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int update(String sql, Object... params) {
        if (sql == null || params.length % 2 != 0) {
            throw new IllegalArgumentException("SQl query should not be null or params should be even");
        }
        for (int i = 0; i < params.length; i += 2) {
            if (!(params[i] instanceof String)) {
                throw new IllegalArgumentException("Even indexed params should be string representing the named parameter.");
            }
            sql = sql.replace(":" + params[i], "?");
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i += 2) {
                statement.setObject(i / 2 + 1, params[i + 1]);
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        if (rowMapper == null) {
            throw new IllegalArgumentException("RowMapper should not be null");
        }
        List<T> results = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                T obj = rowMapper.map(resultSet);
                results.add(obj);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

}
