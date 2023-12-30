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
		Flux<T> output = createTaskFlowPipelineOutput(sink.asFlux(), config);
		return new TaskFlowPipeline<T, T>(sink, output);
	}

	public static <T, R> TaskFlowPipeline<T, R> create(TaskFlowPipelineConfig config,
			TaskPipelineBatchConfig<T, R> batchConfig) {
		Sinks.Many<AsyncTask<T>> sink = createTaskFlowPipelineInput(config);
		Flux<R> output = createTaskFlowPipelineOutput(sink.asFlux(), config) //
				.bufferTimeout(batchConfig.getBufferMaxSize(), batchConfig.getBufferMaxTime()) //
				.map(batch -> batchConfig.getBatchAggregator().aggregate(batch));
		return new TaskFlowPipeline<T, R>(sink, output);
	}

	public static TaskTreePipeline create(TaskTreePipelineConfig config) {
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

	private static <T> Flux<T> createTaskFlowPipelineOutput(Flux<AsyncTask<T>> taskFlux,
			TaskFlowPipelineConfig config) {
		Flux<Publisher<T>> flux;
		if (config.isPreserveSourceOrdering()) {
			flux = taskFlux //
					.flatMapSequential(getMapper(config.getTaskExecutor()));
		} else {
			flux = taskFlux //
					.flatMap(getMapper(config.getTaskExecutor()));
		}
		Scheduler publishOnScheduler = Schedulers.newSingle("TaskPipeline");
		return flux.publishOn(publishOnScheduler) //
				// flatten output
				.flatMap(p -> p) //
				.doOnComplete(() -> publishOnScheduler.dispose());
	}

	private static <T> Function<AsyncTask<T>, Mono<Publisher<T>>> getMapper(Executor taskExecutor) {
		return (AsyncTask<T> task) -> Mono
				.fromFuture(CompletableFuture.supplyAsync(() -> task.execute(), taskExecutor));
	}
}
