package com.wxingyl.es.index;

import com.wxingyl.es.conf.index.DbTableConfigInfo;
import com.wxingyl.es.db.query.TableQueryInfo;
import com.wxingyl.es.db.*;
import com.wxingyl.es.db.query.SqlQueryHandle;
import org.apache.commons.dbutils.ResultSetHandler;
import org.elasticsearch.common.collect.Tuple;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Created by xing on 15/8/24.
 * index type config bean, the result of parsing index_data.yml
 */
public class IndexTypeBean {

    private IndexTypeDesc type;

    private TableQueryInfo masterTable;

    private List<TableBaseInfo> allTableInfo;

    private IndexTypeBean() {
    }

    public IndexTypeDesc getType() {
        return type;
    }

    public TableQueryInfo getMasterTable() {
        return masterTable;
    }

    public List<TableBaseInfo> getTableInfo(String tableName) {
        if (allTableInfo == null) getAllTableInfo();
        List<TableBaseInfo> list = new ArrayList<>();
        allTableInfo.forEach(v -> {
            if (v.getTable().getTable().equals(tableName)) list.add(v);
        });
        return list;
    }

    /**
     * @return unmodifiable list
     */
    public List<TableBaseInfo> getAllTableInfo() {
        if (allTableInfo == null) {
            List<TableBaseInfo> list = new ArrayList<>();
            masterTable.allTableQueryBaseInfo(list);
            allTableInfo = Collections.unmodifiableList(list);
        }
        return allTableInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexTypeBean)) return false;

        IndexTypeBean bean = (IndexTypeBean) o;

        return type.equals(bean.type);

    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {

        private IndexTypeDesc type;

        private Map<DbTableDesc, Tuple<TableQueryInfo.Builder, DbTableFieldDesc>> tableMap = new HashMap<>();

        public Builder type(IndexTypeDesc type) {
            this.type = type;
            return this;
        }

        public Builder addTableQuery(SqlQueryHandle queryHandler, DbTableConfigInfo tableInfo,
                                     ResultSetHandler<List<Map<String, Object>>> rsh) {
            TableQueryInfo.Builder queryBuilder = TableQueryInfo.build();
            queryBuilder.queryHandler(queryHandler)
                    .queryCommon(queryHandler.createPrepareSqlQuery(tableInfo))
                    .rsh(rsh);
            tableMap.put(tableInfo.getTable(), Tuple.tuple(queryBuilder, tableInfo.getMasterField()));
            return this;
        }

        public IndexTypeBean build(DbTableDesc masterTable,
                                   BiConsumer<TableQueryInfo, List<String>> masterAliasVerify) {
            IndexTypeBean bean = new IndexTypeBean();
            bean.type = type;
            tableMap.values().forEach(v -> {
                v.v1().masterAliasVerify(masterAliasVerify);
                DbTableFieldDesc field = v.v2();
                if (field == null) return;
                TableQueryInfo.Builder builder = tableMap.get(field.newDbTableDesc()).v1();
                builder.addSlave(v.v1(), field.getField());
            });
            bean.masterTable = tableMap.get(masterTable).v1().build();
            tableMap.clear();
            return bean;
        }
    }
}