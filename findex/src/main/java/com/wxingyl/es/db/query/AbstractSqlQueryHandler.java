package com.wxingyl.es.db.query;

import com.wxingyl.es.db.DbTableDesc;
import com.wxingyl.es.index.db.SqlQueryCommon;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.elasticsearch.common.cache.CacheBuilder;
import org.elasticsearch.common.cache.CacheLoader;
import org.elasticsearch.common.cache.LoadingCache;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by xing on 15/8/26.
 * abstract sql query handler
 */
public abstract class AbstractSqlQueryHandler implements SqlQueryHandle {

    protected static final MapListHandler DEFAULT_MAP_LIST_HANDLER = new MapListHandler();

    private QueryRunner queryRunner;

    private LoadingCache<String, Set<String>> schemaTablesCache;

    private LoadingCache<DbTableDesc, Set<String>> tableFieldsCache;

    protected SqlQueryStatementStructure queryStatementStructure;

    public AbstractSqlQueryHandler(DataSource dataSource, SqlQueryStatementStructure queryStatementStructure) {
        queryRunner = new QueryRunner(dataSource);
        this.queryStatementStructure = queryStatementStructure;
        schemaTablesCache = CacheBuilder.newBuilder()
                .weakKeys()
                .weakValues()
                .build(new CacheLoader<String, Set<String>>() {
                    @Override
                    public Set<String> load(String schema) throws Exception {
                        return loadAllTables(schema);
                    }
                });
        tableFieldsCache = CacheBuilder.newBuilder()
                .weakKeys()
                .weakValues()
                .build(new CacheLoader<DbTableDesc, Set<String>>() {
                    @Override
                    public Set<String> load(DbTableDesc table) throws Exception {
                        return loadAllFields(table);
                    }
                });
    }

    @Override
    public <T> T query(BaseQueryParam param, ResultSetHandler<T> rsh) throws SQLException {
        return queryRunner.query(createSql(param), rsh);
    }

    protected QueryRunner getQueryRunner() {
        return queryRunner;
    }

    protected abstract String createCountSql(SqlQueryCommon common);

    protected abstract String createSql(BaseQueryParam param);

    protected abstract Set<String> loadAllTables(String schema) throws Exception;

    protected abstract Set<String> loadAllFields(DbTableDesc table) throws Exception;

    @Override
    public Set<String> getAllTables(String schema) throws ExecutionException {
        return schemaTablesCache.get(schema);
    }

    @Override
    public Set<String> getAllFields(DbTableDesc table) throws ExecutionException {
        return tableFieldsCache.get(table);
    }

    @Override
    public long countKeyField(SqlQueryCommon common) throws SQLException {
        ScalarHandler<Long> scalarHandler = new ScalarHandler<>();
        Long num = queryRunner.query(createCountSql(common), scalarHandler);
        return num == null ? 0 : num;
    }
}