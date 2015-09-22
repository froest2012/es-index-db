package com.wxingyl.es.rindex;

import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;

/**
 * Created by xing on 15/9/22.
 * canal connector adapt
 */
public interface CanalConnectorAdapt {

    String getDestination();

    void connect() throws CanalClientException;

    Message getWithoutAck() throws CanalClientException;

    void ack(long batchId) throws CanalClientException;

    void rollback(long batchId) throws CanalClientException;

    void disConnect() throws CanalClientException;

}