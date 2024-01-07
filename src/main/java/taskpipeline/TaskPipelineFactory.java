package taskpipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.ManySpec;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import taskpipeline.config.TaskFlowPipelineConfig;
import taskpipeline.config.TaskPipelineBatchConfig;
import taskpipeline.config.TaskTreePipelineConfig;
import taskpipeline.config.tasktreenode.ITaskTreeNode;

/**
 * Factory for two kinds of pipeline.
 */
public class TaskPipelineFactory {

	private static String SINGLE_THREAD_NAME_PREFIX = "TaskPipeline-output";

	/**
	 * Creates an instance of {@link TaskFlowPipeline} with the given base
	 * configuration without batches.
	 */
	public static <T> TaskFlowPipeline<T, T> create(TaskFlowPipelineConfig config) {
		Sinks.Many<TaskSupplier<T>> sink = createTaskFlowPipelineInput(config);
		Scheduler outputScheduler = Schedulers.newSingle(SINGLE_THREAD_NAME_PREFIX);
		Flux<T> output = createTaskFlowPipelineOutput(sink.asFlux(), config, outputScheduler);
		return new TaskFlowPipeline<T, T>(sink, output);
	}

	/**
	 * Creates an instance of {@link TaskFlowPipeline} with the given base
	 * configuration and the configuration for batches.
	 */
	public static <T, U> TaskFlowPipeline<T, U> create(TaskFlowPipelineConfig config,
			TaskPipelineBatchConfig<T, U> batchConfig) {
		Sinks.Many<TaskSupplier<T>> sink = createTaskFlowPipelineInput(config);
		Scheduler outputScheduler = Schedulers.newSingle(SINGLE_THREAD_NAME_PREFIX);
		Flux<U> output = createTaskFlowPipelineOutput(sink.asFlux(), config, outputScheduler) //
				.bufferTimeout(batchConfig.getBufferMaxSize(), batchConfig.getBufferMaxTime(), outputScheduler) //
				.map(batch -> batchConfig.getBatchAggregator().aggregate(batch));
		return new TaskFlowPipeline<T, U>(sink, output);
	}

	/**
	 * Creates an instance of {@link TaskTreePipeline} with the given configuration.
	 */
	public static <T> TaskTreePipeline<T> create(TaskTreePipelineConfig<T> config) {
		TaskTreePipeline<T> pipeline;
		List<ITaskTreeNode<T, ?>> taskTreeNodes = config.getTaskTreeRootNode().getTaskTreeNodes();
		// TODO
		if (taskTreeNodes.isEmpty()) {
			Flux<T> output = null;
			pipeline = new TaskTreePipeline<T>(output, null);
		} else {
			// first, validate that all deepest (lowermost) task tree nodes are named, i.e.
			// instances of TaskTreeLeafNode. Otherwise, we can not get output streams from
			// the pipeline.
			// TODO

			Map<String, Flux<?>> namedOutputs = new HashMap<>();
			pipeline = new TaskTreePipeline<T>(null, namedOutputs);
		}

		return pipeline;
	}

	private static <T> Sinks.Many<TaskSupplier<T>> createTaskFlowPipelineInput(TaskFlowPipelineConfig config) {
		// Use unsafe sinks which are are not serialized nor thread safe. If needed they
		// must be externally synchronized. This allows the sinks to have less overhead,
		// since they don't care to detect concurrent access anymore.
		ManySpec manySpec = Sinks.unsafe().many();
		return switch (config.getInputSpec()) {
		case UNICAST -> manySpec.unicast().<TaskSupplier<T>>onBackpressureBuffer();
		case MULTICAST -> manySpec.multicast().<TaskSupplier<T>>onBackpressureBuffer();
		case MULTICAST_REPLAY -> manySpec.replay().<TaskSupplier<T>>all();
		};
	}

	private static <T> Flux<T> createTaskFlowPipelineOutput(Flux<TaskSupplier<T>> taskFlux,
			TaskFlowPipelineConfig config, Scheduler outputScheduler) {
		Flux<Publisher<T>> flux;
		if (config.isPreserveSourceOrdering()) {
			flux = taskFlux //
					.flatMapSequential(getMapper(config.getTaskExecutor()));
		} else {
			flux = taskFlux //
					.flatMap(getMapper(config.getTaskExecutor()));
		}
		return flux.publishOn(outputScheduler) //
				// flatten output
				.flatMap(p -> p) //
				.doOnComplete(() -> outputScheduler.dispose());
	}

	private static <T> Function<TaskSupplier<T>, Mono<Publisher<T>>> getMapper(Executor taskExecutor) {
		return (TaskSupplier<T> task) -> Mono
				.fromFuture(CompletableFuture.supplyAsync(() -> task.execute(), taskExecutor));
		// .thenApply((Publisher<T> input) -> {
		// System.out.println("thenApply: " + Thread.currentThread().getId());
		// return input;
		// }));
	}
}
