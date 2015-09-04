package com.wxingyl.es.index;

import com.wxingyl.es.conf.index.TableQueryInfo;
import com.wxingyl.es.exception.IndexDocException;
import com.wxingyl.es.jdal.TableQueryResult;
import com.wxingyl.es.jdal.SqlQueryParam;
import com.wxingyl.es.jdal.handle.SqlQueryHandle;
import com.wxingyl.es.util.CommonUtils;
import org.elasticsearch.common.collect.ImmutableMultimap;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by xing on 15/8/31.
 * Note: thread unsafe
 */
public class TableDependQuery implements Iterator<DbQueryDependResult> {

    private DbQueryDependResult masterResult;

    private SqlQueryParam masterParam;

    private SqlQueryHandle masterQueryHandler;

    private ImmutableMultimap<String, TableQueryInfo> slaveQuery;

    public TableDependQuery(TableQueryInfo masterTable) {
        masterParam = new SqlQueryParam(masterTable);
        masterQueryHandler = masterTable.getQueryHandler();
        slaveQuery = masterTable.getSlaveQuery();
    }

    @Override
    public boolean hasNext() {
        return masterParam.getPage() == 0 || masterResult.needContinue();
    }

    /**
     * query next
     */
    @Override
    public DbQueryDependResult next() {
        try {
            TableQueryResult queryResult = masterQueryHandler.query(masterParam);
            masterResult = new DbQueryDependResult(queryResult);
            slaveQuery(masterResult, slaveQuery);
        } catch (SQLException e) {
            throw new IndexDocException("query data have sqlException from: " + masterParam, e);
        }
        masterParam.addPage();
        return null;
    }

    private void slaveQuery(DbQueryDependResult masterResult, ImmutableMultimap<String, TableQueryInfo> slaveMap) throws SQLException {
        if (masterResult == null || masterResult.isEmpty() || slaveMap == null) return;
        for (String field : slaveMap.keys()) {
            Set<Object> set = masterResult.getValuesForField(field);
            if (set.isEmpty()) continue;
            for (TableQueryInfo slaveTableQuery : slaveMap.get(field)) {
                TableQueryResult slaveRet;
                if (set.size() > slaveTableQuery.getQueryCommon().getPageSize()) {
                    slaveRet = groupQuery(set, slaveTableQuery);
                } else {
                    slaveRet = pageQuery(set, slaveTableQuery);
                }

                if (slaveRet == null || slaveRet.isEmpty()) continue;
                DbQueryDependResult slaveResult = new DbQueryDependResult(slaveRet);

                slaveQuery(slaveResult, slaveTableQuery.getSlaveQuery());

                masterResult.addSlaveResult(field, slaveResult);
            }
        }
    }

    private TableQueryResult pageQuery(Collection<Object> collection, TableQueryInfo queryInfo) throws SQLException {
        SqlQueryParam param = new SqlQueryParam(queryInfo, collection);
        TableQueryResult ret = null;
        do {
            TableQueryResult result = queryInfo.getQueryHandler().query(param);
            ret = ret == null ? result : ret.addPageResult(result);
            param.addPage();
        } while (ret != null && ret.needContinue());
        return ret;
    }

    private TableQueryResult groupQuery(Set<Object> set, TableQueryInfo queryInfo) throws SQLException {
        TableQueryResult ret = null;
        for (List<Object> whereList : CommonUtils.groupList(set, queryInfo.getQueryCommon().getPageSize())) {
            TableQueryResult result = pageQuery(whereList, queryInfo);
            ret = ret == null ? result : ret.addPageResult(result);
        }
        return ret;
    }
}
