package tech.insight.rpc.loadbalance;

import tech.insight.rpc.register.ServiceMetaData;

import java.util.List;

public interface LoadBalance {

    ServiceMetaData select(List<ServiceMetaData> metaDataList);
}
