package com.bondarenko.template;

import com.bondarenko.PrepareStatementExecutor;
import com.bondarenko.mapper.RowMapper;

import javax.sql.DataSource;

public class JdbcTemplate implements JdbcOperations {
    private DataSource dataSource;
    private PrepareStatementExecutor executor;
    private RowMapper mapper;

    public JdbcTemplate(DataSource dataSource) {
    }

    @Override
    public Object query(String sql, RowMapper rowMapper) {
        return null;
    }

    @Override
    public Object queryForObject(String sql, RowMapper rowMapper, Object... params) {
        return null;
    }

    @Override
    public int update(String sql, Object... params) {
        return 0;
    }
}
