package com.bondarenko.template.validation;

import com.bondarenko.mapper.RowMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilsTest {
    @Test
    public void validateParamMap_ShouldThrowException_WhenParamMapIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.validateParamMap(null);
        });
    }

    @Test
    public void validateParamMap_ShouldNotThrowException_WhenParamMapHasEvenSize() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("key1", "value1");
        paramMap.put("key2", "value2");

        assertDoesNotThrow(() -> {
            ValidationUtils.validateParamMap(paramMap);
        });
    }

    @Test
    public void validateParamArray_ShouldThrowException_WhenParamArrayIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.validateParamArray(null);
        });
    }

    @Test
    public void validateSql_ShouldThrowException_WhenSqlIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.validateSql(null);
        });
    }

    @Test
    public void validateRowMapper_ShouldThrowException_WhenRowMapperIsNull() {
        RowMapper<String> rowMapper = null;

        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.validateRowMapper(rowMapper);
        });
    }

    @Test
    public void validateRowMapper_ShouldNotThrowException_WhenRowMapperIsNotNull() {
        RowMapper<String> rowMapper = resultSet -> "result";

        assertDoesNotThrow(() -> {
            ValidationUtils.validateRowMapper(rowMapper);
        });
    }

}