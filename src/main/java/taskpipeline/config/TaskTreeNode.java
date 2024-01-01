package taskpipeline.config;

import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import taskpipeline.TaskFunction;

@Value
@Builder
public class TaskTreeNode<T, U, V> {

	@NonNull
	TaskFunction<T, U> task;

	@Builder.Default
	boolean preserveSourceOrdering = false;

	@Singular("taskTreeNode")
	List<TaskTreeNode<U, ?, ?>> taskTreeNodes;

	TaskPipelineBatchConfig<U, V> batchConfig;
}
