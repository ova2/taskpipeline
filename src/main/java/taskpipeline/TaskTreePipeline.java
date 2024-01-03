package taskpipeline;

import java.util.Map;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import taskpipeline.config.tasktreenode.TaskTreeLeafNode;
import taskpipeline.config.tasktreenode.TaskTreeRootNode;

/**
 * Represents a non-blocking pipeline with the static pre-configured
 * concatenated tasks {@link TaskSupplier<T>} and {@link TaskFunction<T, U>}.
 * Such pre-configured task tree serves as input. The result of task execution
 * of a parent task gets passed as input into all direct child tasks. Every task
 * can be executed in parallel in multiple threads. The results of "leaf" tasks
 * executions can be batched if necessary.
 * 
 * The results of tasks executions for task tree branches are available in form
 * of {@link Flux} streams. Such outputs on task tree branches can occur in
 * different threads. Any upstream broadcasts data to all subscribers along the
 * task tree. The connection to the top upstream source only happens when all
 * subscribers subscribe. The disconnection happens when all subscribers
 * cancelled or the top upstream source completed.
 * 
 * A configured pipeline can be created by {@link TaskPipelineFactory}.
 */
@Value
@Slf4j
public class TaskTreePipeline<T> {

	/**
	 * Provides an output from the root (top) {@link Flux} upstream. This property
	 * is only set if the {@link TaskTreeRootNode} is the only one task tree node in
	 * this pipeline and no other nodes exist.
	 */
	Flux<T> rootOutput;

	/**
	 * Named outputs as {@link Flux} streams. Named outputs are created from
	 * {@link TaskTreeLeafNode}s.
	 */
	@Getter(AccessLevel.NONE)
	Map<String, Flux<?>> outputs;

	/**
	 * Gets a named output for tasks results as {@link Flux} stream. Provided result
	 * type should be specified by the given parameterized {@link Class}.
	 */
	public <O> Flux<O> getNamedOutput(String name, Class<O> clazz) {
		if (outputs == null || !outputs.containsKey(Objects.requireNonNull(name))) {
			log.warn("Named output Flux stream with the name {} doesn't exist", name);
			return null;
		}
		return outputs.get(name).cast(clazz);
	}
}
