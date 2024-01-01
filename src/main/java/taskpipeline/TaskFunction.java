package taskpipeline;

import org.reactivestreams.Publisher;

/**
 * Represents a task in form of function with input and output. The output is a
 * parameterized {@link Publisher<T>} which can be e.g. a
 * {@link reactor.core.publisher.Mono<T>} or a
 * {@link reactor.core.publisher.Flux<T>}.
 */
@FunctionalInterface
public interface TaskFunction<T, U> {

	/**
	 * Executes this task with the given input of type T and returns results in form
	 * of {@link Publisher<U>}.
	 */
	Publisher<U> execute(T input);
}
