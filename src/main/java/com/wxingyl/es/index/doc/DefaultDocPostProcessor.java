package com.wxingyl.es.index.doc;

import com.wxingyl.es.conf.IndexSlaveResultMergeEnum;
import com.wxingyl.es.index.DbQueryDependResult;
import com.wxingyl.es.index.IndexTypeDesc;
import com.wxingyl.es.jdal.TableQueryBaseInfo;
import com.wxingyl.es.jdal.TableQueryResult;
import com.wxingyl.es.util.CommonUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xing on 15/9/6.
 * default DocPostProcessor
 */
public class DefaultDocPostProcessor extends AbstractDocPostProcessor {

    @Override
    public PageDocument postProcessor(DbQueryDependResult masterResult) {
        return null;
    }

    @Override
    public PageDocument initMasterPageDoc(TableQueryResult masterResult) {
        PageDocument masterPageDoc = new PageDocument(DocumentBaseInfo.build(masterResult.getBaseInfo(), getPostEvn()));
        masterPageDoc.addAll(DocFields.build(masterResult.getDbData()));
        return masterPageDoc;
    }

    @Override
    public PageDocument applyTableQueryResult(PageDocument masterPageDoc, String masterField, TableQueryResult slaveResult) {
        TableQueryBaseInfo slaveBaseInfo = slaveResult.getBaseInfo();
        Map<Object, List<Map<String, Object>>> group = CommonUtils.groupListMap(slaveResult.getDbData(),
                slaveBaseInfo.getKeyField(), true);
        String keyAlias = slaveBaseInfo.getMasterAlias();
        IndexSlaveResultMergeEnum mergeType = slaveBaseInfo.getMergeType();
        masterPageDoc.forEach(doc -> {
            Object obj = doc.get(masterField);
            if (obj != null) {
                Object valObj = mergeType.function(group.get(obj));
                if (valObj != null) doc.put(keyAlias, valObj);
            }
        });
        return masterPageDoc;
    }

    @Override
    public PageDocument mergeChildPageDoc(PageDocument masterPageDoc, String masterField, PageDocument childPageDoc) {
        Map<Object, List<DocFields>> group = childPageDoc.groupByKeyField(true);
        String masterAlias = childPageDoc.getBaseInfo().getMasterAlias();
        IndexSlaveResultMergeEnum mergeType = childPageDoc.getBaseInfo().getMergeType();
        masterPageDoc.forEach(doc -> {
            Object val = doc.get(masterField);
            if (val != null) {
                Object obj = mergeType.function(group.get(val));
                if (obj != null) doc.put(masterAlias, mergeType.function(group.get(val)));
            }
        });
        return masterPageDoc;
    }

    @Override
    public Set<IndexTypeDesc> supportType() {
        return null;
    }
}