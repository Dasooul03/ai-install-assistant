package com.example.installassistant.operation;

import com.example.installassistant.intent.IntentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OperationDispatcherTest {

    @Test
    void shouldDispatchToCorrectHandler() {
        CreateClusterHandler clusterHandler = new CreateClusterHandler();
        CreatePartitionHandler partitionHandler = new CreatePartitionHandler();
        AddInstanceHandler addInstanceHandler = new AddInstanceHandler();
        ServiceLifecycleHandler lifecycleHandler = new ServiceLifecycleHandler();

        OperationDispatcher dispatcher = new OperationDispatcher(
                List.of(clusterHandler, partitionHandler, addInstanceHandler, lifecycleHandler),
                null
        );

        // Test CREATE_CLUSTER
        OperationRequest req1 = new OperationRequest(1L, IntentType.CREATE_CLUSTER.name(),
                Map.of("nodeCount", "5"), "创建集群");
        OperationResult result1 = dispatcher.dispatch(req1);
        assertThat(result1.getStatus()).isEqualTo("SUCCESS");
        assertThat(result1.getOperationName()).isEqualTo("createCluster");

        // Test ADD_INSTANCE
        OperationRequest req2 = new OperationRequest(1L, IntentType.ADD_INSTANCE.name(),
                Map.of("count", "3"), "增加实例");
        OperationResult result2 = dispatcher.dispatch(req2);
        assertThat(result2.getStatus()).isEqualTo("SUCCESS");
        assertThat(result2.getMessage()).contains("3");

        // Test unknown intent
        OperationRequest req3 = new OperationRequest(1L, "UNKNOWN", Map.of(), "unknown");
        OperationResult result3 = dispatcher.dispatch(req3);
        assertThat(result3.getStatus()).isEqualTo("FAILED");
    }

    @Test
    void eachHandlerShouldMatchCorrectIntent() {
        assertThat(new CreateClusterHandler().canHandle(IntentType.CREATE_CLUSTER)).isTrue();
        assertThat(new CreateClusterHandler().canHandle(IntentType.CREATE_PARTITION)).isFalse();
        assertThat(new CreatePartitionHandler().canHandle(IntentType.CREATE_PARTITION)).isTrue();
        assertThat(new AddInstanceHandler().canHandle(IntentType.ADD_INSTANCE)).isTrue();
        assertThat(new ServiceLifecycleHandler().canHandle(IntentType.SERVICE_LIFECYCLE)).isTrue();
        assertThat(new ServiceLifecycleHandler().canHandle(IntentType.CREATE_CLUSTER)).isFalse();
    }
}
