package taskpipeline;

import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Represents a non-blocking pipeline for the tasks {@link TaskSupplier<T>}.
 * Several tasks can be emitted into the pipeline via an input interface, then
 * executed in parallel in multiple threads, and batched if necessary. The
 * results of tasks executions are available in form of {@link Flux} stream. The
 * input and output occur in different threads. Execution of ongoing tasks in
 * multiple threads can significantly enhance performance and efficiency in
 * specific scenarios.
 * 
 * A configured pipeline can be created by {@link TaskPipelineFactory}.
 */
@Value
public class TaskFlowPipeline<T, U> {

	/**
	 * Provides an input interface for tasks to be executed.
	 */
	Sinks.Many<TaskSupplier<T>> input;

	/**
	 * Provides an output for tasks results as {@link Flux} stream.
	 */
	Flux<U> output;
}
