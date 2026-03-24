package com.zjb.mjgl.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        Object obj = rs.getObject(columnName);
        if (obj == null) return null;

        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate();
        }
        if (obj instanceof Timestamp) {
            Timestamp ts = (Timestamp) obj;
            return ts.toLocalDateTime().toLocalDate();
        }
        if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        }
        if (obj instanceof java.time.LocalDateTime) {
            java.time.LocalDateTime ldt = (java.time.LocalDateTime) obj;
            return ldt.toLocalDate();
        }
        if (obj instanceof java.util.Date) {
            java.util.Date ud = (java.util.Date) obj;
            return ud.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        // 兜底：部分驱动的 getObject 返回字符串
        String s = rs.getString(columnName);
        if (s == null || s.trim().isEmpty()) return null;
        try {
            // 兼容 'yyyy-MM-dd' 和 'yyyy-MM-dd HH:mm:ss'
            String normalized = s.length() >= 10 ? s.substring(0, 10) : s;
            return LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new SQLException("LocalDate parse failed, column=" + columnName + ", value=" + s, e);
        }
    }

    @Override
    public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        if (obj == null) return null;

        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate();
        }
        if (obj instanceof Timestamp) {
            Timestamp ts = (Timestamp) obj;
            return ts.toLocalDateTime().toLocalDate();
        }
        if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        }
        if (obj instanceof java.time.LocalDateTime) {
            java.time.LocalDateTime ldt = (java.time.LocalDateTime) obj;
            return ldt.toLocalDate();
        }
        if (obj instanceof java.util.Date) {
            java.util.Date ud = (java.util.Date) obj;
            return ud.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String s = rs.getString(columnIndex);
        if (s == null || s.trim().isEmpty()) return null;
        try {
            String normalized = s.length() >= 10 ? s.substring(0, 10) : s;
            return LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new SQLException("LocalDate parse failed, columnIndex=" + columnIndex + ", value=" + s, e);
        }
    }

    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        if (obj == null) return null;

        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate();
        }
        if (obj instanceof Timestamp) {
            Timestamp ts = (Timestamp) obj;
            return ts.toLocalDateTime().toLocalDate();
        }
        if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        }
        if (obj instanceof java.time.LocalDateTime) {
            java.time.LocalDateTime ldt = (java.time.LocalDateTime) obj;
            return ldt.toLocalDate();
        }
        if (obj instanceof java.util.Date) {
            java.util.Date ud = (java.util.Date) obj;
            return ud.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String s = cs.getString(columnIndex);
        if (s == null || s.trim().isEmpty()) return null;
        try {
            String normalized = s.length() >= 10 ? s.substring(0, 10) : s;
            return LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            throw new SQLException("LocalDate parse failed, columnIndex=" + columnIndex + ", value=" + s, e);
        }
    }
}
