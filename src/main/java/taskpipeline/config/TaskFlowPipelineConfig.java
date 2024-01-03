package taskpipeline.config;

import java.util.concurrent.Executor;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Configuration for {@link taskpipeline.TaskFlowPipeline}.
 */
@Value
@Builder
public class TaskFlowPipelineConfig {

	/**
	 * {@link Executor} used for parallel task execution.
	 */
	@NonNull
	Executor taskExecutor;

	/**
	 * Boolean flag if the input source ordering should be preserved for output.
	 */
	@Builder.Default
	boolean preserveSourceOrdering = false;

	/**
	 * Type of input interface (sinks) which can emit multiple tasks.
	 */
	@Builder.Default
	TaskPipelineInputSpec inputSpec = TaskPipelineInputSpec.UNICAST;
}
