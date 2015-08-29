package com.wxingyl.es.jdal;

/**
 * Created by xing on 15/8/27.
 * query data from table, the sql has common part, eg:
 *  SELECT XX, YY, ZZ FROM TABLE_NAME
 * so we can prepare create common part, no need to create every query
 */
public class PrepareSqlQuery {

    private String commonFormatSql;

    private boolean containWhere;

    private int limit;

    private String orderBy;

    public String getOrderBy() {
        return orderBy;
    }

    public int getLimit() {
        return limit;
    }

    public String getCommonFormatSql() {
        return commonFormatSql;
    }

    public boolean isContainWhere() {
        return containWhere;
    }

    public String formatSql(Object... args) {
        return String.format(commonFormatSql, args);
    }

    public static Build build() {
        return new Build();
    }

    public static class Build {

        private String commonFormatSql;

        private boolean containWhere;

        public Build commonFormatSql(String commonFormatSql) {
            this.commonFormatSql = commonFormatSql;
            return this;
        }

        public Build containWhere() {
            this.containWhere = true;
            return this;
        }

        public PrepareSqlQuery build() {
            PrepareSqlQuery prepareSqlQuery = new PrepareSqlQuery();
            prepareSqlQuery.commonFormatSql = commonFormatSql;
            prepareSqlQuery.containWhere = containWhere;
            return prepareSqlQuery;
        }
    }
}