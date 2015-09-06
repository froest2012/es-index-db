package com.wxingyl.es.jdal;

import java.util.List;
import java.util.Map;

/**
 * Created by xing on 15/8/30.
 * query db result
 */
public class TableQueryResult {

    private int pageSize;

    private DbTableDesc table;

    /**
     * the field is primary key, its value should be unique in table
     */
    private String keyField;

    private String masterAlias;

    /**
     * can not null
     */
    private List<Map<String, Object>> dbData;

    private TableQueryResult() {}

    public int getPageSize() {
        return pageSize;
    }

    public DbTableDesc getTable() {
        return table;
    }

    public String getKeyField() {
        return keyField;
    }

    public String getMasterAlias() {
        return masterAlias;
    }

    public boolean isEmpty() {
        return dbData.isEmpty();
    }

    public boolean needContinue() {
        return dbData.size() == pageSize;
    }

    public List<Map<String, Object>> getDbData() {
        return dbData;
    }

    public TableQueryResult addPageResult(TableQueryResult slaveRet) {
        if (!(slaveRet == null || this == slaveRet || slaveRet.isEmpty())) {
            dbData.addAll(slaveRet.dbData);
        }
        return this;
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private int pageSize;

        private DbTableDesc table;

        private String keyField;

        private String masterAlias;

        private List<Map<String, Object>> dbData;

        public Builder sqlQueryCommon(SqlQueryCommon common) {
            pageSize = common.getPageSize();
            table = common.getTable();
            keyField = common.getKeyField();
            masterAlias = common.getMasterAlias();
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder keyField(String keyField) {
            this.keyField = keyField;
            return this;
        }

        public Builder masterAlias(String masterAlias) {
            this.masterAlias = masterAlias;
            return this;
        }

        public Builder table(DbTableDesc table) {
            this.table = table;
            return this;
        }

        public Builder dbData(List<Map<String, Object>> dbData) {
            this.dbData = dbData;
            return this;
        }

        public TableQueryResult build() {
            TableQueryResult ret = new TableQueryResult();
            ret.pageSize = pageSize;
            ret.table = table;
            ret.keyField = keyField;
            ret.dbData = dbData;
            ret.masterAlias = masterAlias;
            return ret;
        }

    }

    private BaseInfo baseInfo;

    public BaseInfo getBaseInfo() {
        if (baseInfo == null) {
            baseInfo = new BaseInfo();
            baseInfo.table = table;
            baseInfo.keyField = keyField;
            baseInfo.masterAlias = masterAlias;
        }
        return baseInfo;
    }

    /**
     * Created by xing on 15/9/7.
     */
    public static class BaseInfo {

        private DbTableDesc table;

        /**
         * the field is primary key, its value should be unique in table
         */
        private String keyField;

        private String masterAlias;

        public DbTableDesc getTable() {
            return table;
        }

        public String getKeyField() {
            return keyField;
        }

        public String getMasterAlias() {
            return masterAlias;
        }
    }
}
