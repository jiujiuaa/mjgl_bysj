package com.zjb.mjgl.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * MyBatis LocalDate 与 JDBC DATE/TIMESTAMP 映射
 */
@MappedTypes(LocalDate.class)
@MappedJdbcTypes({JdbcType.DATE, JdbcType.TIMESTAMP})
public class LocalDateTypeHandler extends BaseTypeHandler<LocalDate> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        java.util.Date d = rs.getDate(columnName);
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        java.util.Date d = rs.getDate(columnIndex);
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        java.util.Date d = cs.getDate(columnIndex);
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
