package taskpipeline.config;

import java.util.List;
import java.util.concurrent.Executor;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import reactor.core.scheduler.Scheduler;
import taskpipeline.TaskSupplier;
import taskpipeline.TaskTreePipeline;

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
	 * Task for the root node of the task tree.
	 */
	@NonNull
	TaskSupplier<T> rootTask;

	/**
	 * Boolean flag for the root task if the input source ordering should be
	 * preserved for output.
	 */
	@Builder.Default
	boolean preserveSourceOrdering = false;

	/**
	 * Collection of {@link TaskTreeNode}s as configurations for direct child tasks
	 * of the root task.
	 */
	@Singular("taskTreeNode")
	List<TaskTreeNode<T, ?, ?>> taskTreeNodes;
}
