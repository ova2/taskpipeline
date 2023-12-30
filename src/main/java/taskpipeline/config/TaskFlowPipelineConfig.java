package main.java.taskpipeline.config;

import java.util.concurrent.Executor;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class TaskFlowPipelineConfig {

	@NonNull
	Executor taskExecutor;

	@Builder.Default
	boolean preserveSourceOrdering = false;

	@Builder.Default
	TaskPipelineSpec pipelineSpec = TaskPipelineSpec.UNICAST;
}
