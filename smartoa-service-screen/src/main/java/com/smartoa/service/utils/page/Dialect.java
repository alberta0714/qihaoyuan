package com.smartoa.service.utils.page;

/**
 * @Author hbb
 * @Date 2016/12/9 16:24
 */
public abstract class Dialect {
    public abstract String getLimitString(String paramString, int paramInt1,
                                          int paramInt2);

    public static enum Type {
        MYSQL,ORACLE;
    }
}
