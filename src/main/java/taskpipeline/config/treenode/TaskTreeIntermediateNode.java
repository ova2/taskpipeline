package taskpipeline.config.treenode;

import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import taskpipeline.TaskFunction;

/**
 * Configuration for nodes in a task tree which are not root and not leaf nodes.
 */
@Value
@Builder
public class TaskTreeIntermediateNode<T, U> implements ITaskTreeNode<T, U> {

	/**
	 * Task to be executed.
	 */
	@NonNull
	TaskFunction<T, U> task;

	/**
	 * Collection of {@link ITaskTreeNode}s as configurations for direct children of
	 * this node.
	 */
	@Singular("taskTreeNode")
	List<ITaskTreeNode<U, ?>> taskTreeNodes;
}
