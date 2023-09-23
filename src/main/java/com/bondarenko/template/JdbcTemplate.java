package com.bondarenko.template;

import com.bondarenko.mapper.ResultSetMapper;
import com.bondarenko.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate implements JdbcOperations {
    private final DataSource dataSource;
    private final ResultSetMapper resultSetMapper;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.resultSetMapper = new ResultSetMapper<>();
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            return resultSetMapper.mapResultSetToList(resultSet, rowMapper);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            ResultSet rs = ps.executeQuery();
            return (T) resultSetMapper.mapResultSetToEntity(rs, rowMapper);

        } catch (SQLException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public int update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
