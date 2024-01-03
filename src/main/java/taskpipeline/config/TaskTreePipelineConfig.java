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
import taskpipeline.config.treenode.ITaskTreeNode;

/**
 * Configuration for {@link TaskTreePipeline}.
 */
@Value
@Builder
public class TaskTreePipelineConfig<T> {

	/**
	 * Task for the root node of the task tree.
	 */
	@NonNull
	TaskSupplier<T> rootTask;

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
	 * Boolean flag if the input source ordering should be preserved for output.
	 */
	@Builder.Default
	boolean preserveSourceOrdering = false;

	/**
	 * Collection of {@link ITaskTreeNode}s as configurations for direct children of
	 * the root node.
	 */
	@Singular("taskTreeNode")
	List<ITaskTreeNode<T, ?>> taskTreeNodes;
}
