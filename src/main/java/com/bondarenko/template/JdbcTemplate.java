package com.bondarenko.template;

import com.bondarenko.mapper.ResultSetMapper;
import com.bondarenko.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;

public class JdbcTemplate<T> implements JdbcOperations<T> {
    private DataSource dataSource;
    private ResultSetMapper resultSetMapper;
    private PreparedStatement statement;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
        this.resultSetMapper = new ResultSetMapper<>();
    }

    @Override
    public List<T> query(String sql, RowMapper<T> rowMapper) {
        return null;
    }

    @Override
    public T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return null;
    }

    @Override
    public int update(String sql, Object... params) {
        return 0;
    }
}
