package com.smartoa.service.utils.page;

/**
 * @Author hbb
 * @Date 2016/12/9 16:29
 */
public class OracleDialect extends Dialect {
    public String getLimitString(String sql, int offset, int limit) {
        sql = sql.trim();

        StringBuffer pagingSelect = new StringBuffer(sql.length() + 100);

        pagingSelect.append("select * from ( select row_.*, rownum rn from ( ");

        pagingSelect.append(sql);

        pagingSelect.append(" ) row_ where rownum<= " + (offset + limit)
                + ") where rn > " + offset);

        return pagingSelect.toString();
    }
}
