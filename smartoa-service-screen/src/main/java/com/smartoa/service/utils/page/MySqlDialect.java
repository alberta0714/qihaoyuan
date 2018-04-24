package com.smartoa.service.utils.page;

import org.springframework.util.Assert;

/**
 * @Author hbb
 * @Date 2016/12/9 16:26
 */
@SuppressWarnings("unused")
public class MySqlDialect extends Dialect {
    StringBuffer sbSql;
	private  String COUNT_PFRI = "count(";
    private  String INSERT_PFRI = "insert ";
    private  String UPDATE_PFRI = "update ";
    private  String DELETE_PFRI = "delete ";

    public MySqlDialect() {
        this.sbSql = new StringBuffer();
        this.COUNT_PFRI = "count(";
        this.INSERT_PFRI = "insert ";
        this.UPDATE_PFRI = "update ";
        this.DELETE_PFRI = "delete ";
    }

    public String getLimitString(String sql, int skipResults, int maxResults) {
        Assert.notNull(sql, "Sql is not exist!~");

        sql = sql.trim();

        if (sqlFilter(sql))
            return sql;

        this.sbSql.append(sql).append(" LIMIT ").append(skipResults)
                .append(", ").append(maxResults);

        return this.sbSql.toString();
    }

    private boolean sqlFilter(String sql) {
        return ((sql.contains("count("))
                || (sql.contains("count(".toUpperCase()))
                || (sql.contains("insert "))
                || (sql.contains("insert ".toUpperCase()))
                || (sql.contains("update "))
                || (sql.contains("update ".toUpperCase()))
                || (sql.contains("delete ")) || (sql.contains("delete "
                .toUpperCase())));
    }
}
