package taskpipeline.config.tasktreenode;

import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import taskpipeline.TaskSupplier;

/**
 * Configuration for the root node in a task tree.
 */
@Value
@Builder
public class TaskTreeRootNode<T> {

	/**
	 * Task for the root node of the task tree.
	 */
	@NonNull
	TaskSupplier<T> task;

	/**
	 * Collection of {@link ITaskTreeNode}s as configurations for direct children of
	 * the root node.
	 */
	@Singular("taskTreeNode")
	List<ITaskTreeNode<T, ?>> taskTreeNodes;
}
