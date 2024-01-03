package taskpipeline.config.tasktreenode;

import taskpipeline.TaskFunction;

/**
 * Interface for intermediate and leaf nodes in a task tree.
 */
public interface ITaskTreeNode<T, U> {

	/**
	 * Gets task function to be executed.
	 */
	TaskFunction<T, U> getTask();
}
