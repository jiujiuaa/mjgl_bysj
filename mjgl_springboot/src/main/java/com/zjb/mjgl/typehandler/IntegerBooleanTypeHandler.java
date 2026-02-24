package com.zjb.mjgl.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库 int(1=是/0=否) 与 Java Boolean 映射
 */
@MappedTypes(Boolean.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class IntegerBooleanTypeHandler extends BaseTypeHandler<Boolean> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, Boolean.TRUE.equals(parameter) ? 1 : 0);
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int v = rs.getInt(columnName);
        return rs.wasNull() ? null : (v == 1);
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int v = rs.getInt(columnIndex);
        return rs.wasNull() ? null : (v == 1);
    }

    @Override
    public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int v = cs.getInt(columnIndex);
        return cs.wasNull() ? null : (v == 1);
    }
}
