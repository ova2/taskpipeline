package main.java.taskpipeline;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.reactivestreams.Publisher;

import main.java.taskpipeline.config.TaskFlowPipelineConfig;
import main.java.taskpipeline.config.TaskPipelineBatchConfig;
import main.java.taskpipeline.config.TaskTreePipelineConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.ManySpec;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class TaskPipelineFactory {

	public static <T> TaskFlowPipeline<T, T> create(TaskFlowPipelineConfig config) {
		Sinks.Many<AsyncTask<T>> sink = createTaskFlowPipelineInput(config);
		Scheduler outputScheduler = Schedulers.newSingle("TaskPipeline-output");
		Flux<T> output = createTaskFlowPipelineOutput(sink.asFlux(), config, outputScheduler);
		return new TaskFlowPipeline<T, T>(sink, output);
	}

	public static <T, R> TaskFlowPipeline<T, R> create(TaskFlowPipelineConfig config,
			TaskPipelineBatchConfig<T, R> batchConfig) {
		Sinks.Many<AsyncTask<T>> sink = createTaskFlowPipelineInput(config);
		Scheduler outputScheduler = Schedulers.newSingle("TaskPipeline-output");
		Flux<R> output = createTaskFlowPipelineOutput(sink.asFlux(), config, outputScheduler) //
				.bufferTimeout(batchConfig.getBufferMaxSize(), batchConfig.getBufferMaxTime(), outputScheduler) //
				.map(batch -> batchConfig.getBatchAggregator().aggregate(batch));
		return new TaskFlowPipeline<T, R>(sink, output);
	}

	public static TaskTreePipeline create(TaskTreePipelineConfig config) {
		// just an usage example
		TaskTreePipeline pipeline = new TaskTreePipeline(null);
		Flux<Integer> flux = pipeline.getOutput("test", Integer.class);

		// TODO
		return null;
	}

	private static <T> Sinks.Many<AsyncTask<T>> createTaskFlowPipelineInput(TaskFlowPipelineConfig config) {
		ManySpec manySpec = Sinks.many();
		return switch (config.getPipelineSpec()) {
		case UNICAST -> manySpec.unicast().<AsyncTask<T>>onBackpressureBuffer();
		case MULTICAST -> manySpec.multicast().<AsyncTask<T>>onBackpressureBuffer();
		case MULTICAST_REPLAY -> manySpec.replay().<AsyncTask<T>>all();
		};
	}

	private static <T> Flux<T> createTaskFlowPipelineOutput(Flux<AsyncTask<T>> taskFlux, TaskFlowPipelineConfig config,
			Scheduler outputScheduler) {
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

	private static <T> Function<AsyncTask<T>, Mono<Publisher<T>>> getMapper(Executor taskExecutor) {
		return (AsyncTask<T> task) -> Mono
				.fromFuture(CompletableFuture.supplyAsync(() -> task.execute(), taskExecutor));
	}
}
