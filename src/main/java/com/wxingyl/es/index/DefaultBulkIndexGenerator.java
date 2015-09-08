package com.wxingyl.es.index;

import com.wxingyl.es.exception.IndexDocException;
import com.wxingyl.es.index.doc.DocFields;
import com.wxingyl.es.index.doc.DocumentBaseInfo;
import com.wxingyl.es.util.DefaultDateConvert;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by xing on 15/9/7.
 * default BulkIndexGenerate
 */
public class DefaultBulkIndexGenerator extends AbstractBulkIndexGenerator {

    public DefaultBulkIndexGenerator() {
        super();
        setDateConvert(DefaultDateConvert.INSTANCE);
    }

    @Override
    public Set<IndexTypeDesc> supportType() {
        return null;
    }

    @Override
    protected String getDocId(DocumentBaseInfo baseInfo, DocFields doc) {
        return doc.get(baseInfo.getKeyField()).toString();
    }

    @Override
    protected int handleFailed(BulkRequestBuilder bulkRequest, BulkResponse response) {
        List<BulkItemResponse> failedItems = new ArrayList<>(10);
        int failedCount = 0;
        for (BulkItemResponse item : response) {
            if (item.isFailed()) {
                if (failedCount < 10) {
                    failedItems.add(item);
                }
                failedCount++;
            }
        }
        if (failedCount > (bulkRequest.numberOfActions() >> 1)) {
            BulkResponse failResponse = new BulkResponse(failedItems.toArray(new BulkItemResponse[failedItems.size()]), 0);
            throw new IndexDocException(failResponse.buildFailureMessage());
        }
        return failedCount;
    }
}
