package taskpipeline;

import java.util.Map;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import reactor.core.publisher.Flux;

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
public class TaskTreePipeline {

	@Getter(AccessLevel.NONE)
	Map<String, Flux<?>> outputs;

	/**
	 * Gets a named output for tasks results as {@link Flux} stream. Provided result
	 * type should be specified by the given parameterized {@link Class}.
	 */
	public <T> Flux<T> getOutput(String key, Class<T> clazz) {
		if (!outputs.containsKey(Objects.requireNonNull(key))) {
			return null;
		}
		return outputs.get(key).cast(clazz);
	}
}
