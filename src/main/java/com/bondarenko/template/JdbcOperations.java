package com.bondarenko.template;

import com.bondarenko.mapper.RowMapper;

public interface JdbcOperations<T> {
    T query(String sql, RowMapper<T> rowMapper);
    T queryForObject(String sql, RowMapper<T> rowMapper, Object... params);

    int update(String sql, Object... params);
}
