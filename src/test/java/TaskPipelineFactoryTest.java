package test.java;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import main.java.taskpipeline.AsyncTask;
import main.java.taskpipeline.TaskFlowPipeline;
import main.java.taskpipeline.TaskPipelineFactory;
import main.java.taskpipeline.config.TaskFlowPipelineConfig;
import main.java.taskpipeline.config.TaskPipelineBatchConfig;
import main.java.taskpipeline.config.TaskPipelineSpec;
import reactor.core.publisher.Sinks;

@Slf4j
class TaskPipelineFactoryTest {

	@Test
	void createTaskFlowPipeline_shouldOutputResultsInInputOrder_whenPreserveSourceOrderingWasSet() {
		int parallelism = Math.max((int) Math.floor(ForkJoinPool.getCommonPoolParallelism() / 2f), 2);
		ExecutorService executorService = Executors.newWorkStealingPool(parallelism);

		TaskFlowPipeline<AsyncTaskResult, AsyncTaskResult> pipeline = TaskPipelineFactory
				.create(TaskFlowPipelineConfig.builder() //
						.pipelineSpec(TaskPipelineSpec.UNICAST) //
						.preserveSourceOrdering(true) //
						.taskExecutor(executorService) //
						.build());

		long ms = System.currentTimeMillis();
		pipeline.getOutput() //
				.doOnComplete(() -> log.info("Finished in {} sec.", (System.currentTimeMillis() - ms) / 1000f)) //
				.subscribe(taskResult -> log.info("Output task result {}", taskResult.getName()));

		Sinks.Many<AsyncTask<AsyncTaskResult>> input = pipeline.getInput();
		char name = 'A';
		for (int i = 0; i < 100; i++) {
			input.tryEmitNext(ConcreteAsyncTask.builder() //
					.name(String.valueOf((char) ((int) name + i))) //
					.build());
		}
		input.tryEmitComplete();

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	void createTaskFlowPipeline_shouldBatchAndAggregateOutputResults_whenBatchConfigurationAvailable() {
		int parallelism = Math.max((int) Math.floor(ForkJoinPool.getCommonPoolParallelism() / 2f), 2);
		ExecutorService executorService = Executors.newWorkStealingPool(parallelism);

		TaskFlowPipeline<AsyncTaskResult, String> pipeline = TaskPipelineFactory.create( //
				TaskFlowPipelineConfig.builder() //
						.pipelineSpec(TaskPipelineSpec.UNICAST) //
						.taskExecutor(executorService) //
						.build(),
				TaskPipelineBatchConfig.<AsyncTaskResult, String>builder() //
						.bufferMaxSize(10) //
						.bufferMaxTime(Duration.ofMillis(250)) //
						.batchAggregator(batch -> String.join(",", batch.stream() //
								.map(AsyncTaskResult::getName) //
								.toList()))
						.build());

		long ms = System.currentTimeMillis();
		pipeline.getOutput() //
				.doOnComplete(() -> log.info("Finished in {} sec.", (System.currentTimeMillis() - ms) / 1000f)) //
				.subscribe(taskResult -> log.info("Output task result {}", taskResult));

		Sinks.Many<AsyncTask<AsyncTaskResult>> input = pipeline.getInput();
		char name = 'A';
		for (int i = 0; i < 100; i++) {
			input.tryEmitNext(ConcreteAsyncTask.builder() //
					.name(String.valueOf((char) ((int) name + i))) //
					.build());
		}
		input.tryEmitComplete();

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
