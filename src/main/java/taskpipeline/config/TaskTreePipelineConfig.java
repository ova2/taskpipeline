package taskpipeline.config;

import java.util.concurrent.Executor;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import reactor.core.scheduler.Scheduler;
import taskpipeline.TaskTreePipeline;
import taskpipeline.config.tasktreenode.TaskTreeRootNode;

/**
 * Configuration for {@link TaskTreePipeline}.
 */
@Value
@Builder
public class TaskTreePipelineConfig<T> {

	/**
	 * {@link Executor} used for parallel task execution.
	 */
	@NonNull
	Executor taskExecutor;

	/**
	 * {@link Executor} used for output results. If not defined, a separate single
	 * threaded {@link Scheduler} is used per task tree output branch.
	 */
	Executor outputExecutor;

	/**
	 * Boolean flag if the input source ordering should be preserved for output(s).
	 */
	@Builder.Default
	boolean preserveSourceOrdering = false;

	/**
	 * Configurations for the root node in the task tree.
	 */
	@NonNull
	TaskTreeRootNode<T> taskTreeRootNode;
}
