package taskpipeline.config.tasktreenode;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import reactor.core.publisher.Flux;
import taskpipeline.TaskFunction;
import taskpipeline.config.TaskPipelineBatchConfig;

/**
 * Configuration for leaf nodes in a task tree.
 */
@Value
@Builder
public class TaskTreeLeafNode<T, U, V> implements ITaskTreeNode<T, U> {

	/**
	 * Name for named output. Names are used for accessing outputs in form of
	 * {@link Flux} streams.
	 */
	@NonNull
	String name;

	/**
	 * Task to be executed.
	 */
	@NonNull
	TaskFunction<T, U> task;

	/**
	 * Configuration for batched output.
	 */
	TaskPipelineBatchConfig<U, V> batchConfig;
}
