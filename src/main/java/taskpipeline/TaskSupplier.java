package taskpipeline;

import org.reactivestreams.Publisher;

/**
 * Represents a task in form of supplier of results. There is no input and the
 * output is a parameterized {@link Publisher<T>} which can be e.g. a
 * {@link reactor.core.publisher.Mono<T>} or a
 * {@link reactor.core.publisher.Flux<T>}.
 */
@FunctionalInterface
public interface TaskSupplier<T> {

	/**
	 * Executes this task and returns results in form of {@link Publisher<T>}.
	 */
	Publisher<T> execute();
}
