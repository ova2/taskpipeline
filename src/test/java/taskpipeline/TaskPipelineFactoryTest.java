package taskpipeline;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import taskpipeline.config.TaskFlowPipelineConfig;
import taskpipeline.config.TaskPipelineBatchConfig;
import taskpipeline.config.TaskPipelineSpec;
import taskpipeline.config.TaskTreePipelineConfig;
import taskpipeline.config.tasktreenode.TaskTreeIntermediateNode;
import taskpipeline.config.tasktreenode.TaskTreeLeafNode;

@Slf4j
class TaskPipelineFactoryTest {

	@Test
	void createTaskFlowPipeline_shouldOutputResultsInInputOrder_whenPreserveSourceOrderingWasSet() {
		int parallelism = Math.max((int) Math.floor(ForkJoinPool.getCommonPoolParallelism() / 2f), 2);
		ExecutorService executorService = Executors.newWorkStealingPool(parallelism);

		TaskFlowPipeline<TaskResult, TaskResult> pipeline = TaskPipelineFactory.create(TaskFlowPipelineConfig.builder() //
				.pipelineSpec(TaskPipelineSpec.UNICAST) //
				.preserveSourceOrdering(true) //
				.taskExecutor(executorService) //
				.build());

		long ms = System.currentTimeMillis();
		pipeline.getOutput() //
				.doOnComplete(() -> log.info("Finished in {} sec.", (System.currentTimeMillis() - ms) / 1000f)) //
				.subscribe(taskResult -> log.info("Output task result {}", taskResult.getName()));

		Sinks.Many<TaskSupplier<TaskResult>> input = pipeline.getInput();
		char name = 'A';
		for (int i = 0; i < 100; i++) {
			input.tryEmitNext(ConcreteTaskSupplier.builder() //
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

		TaskFlowPipeline<TaskResult, String> pipeline = TaskPipelineFactory.create( //
				TaskFlowPipelineConfig.builder() //
						.pipelineSpec(TaskPipelineSpec.UNICAST) //
						.taskExecutor(executorService) //
						.build(),
				TaskPipelineBatchConfig.<TaskResult, String>builder() //
						.bufferMaxSize(10) //
						.bufferMaxTime(Duration.ofMillis(250)) //
						.batchAggregator(batch -> String.join(",", batch.stream() //
								.map(TaskResult::getName) //
								.toList()))
						.build());

		long ms = System.currentTimeMillis();
		pipeline.getOutput() //
				.doOnComplete(() -> log.info("Finished in {} sec.", (System.currentTimeMillis() - ms) / 1000f)) //
				.subscribe(taskResult -> log.info("Output task result {}", taskResult));

		Sinks.Many<TaskSupplier<TaskResult>> input = pipeline.getInput();
		char name = 'A';
		for (int i = 0; i < 100; i++) {
			input.tryEmitNext(ConcreteTaskSupplier.builder() //
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

	/**
	 * Create and test a TaskTreePipeline with a deep = 3.
	 * 
	 * <pre>
	 * Root Task
	 *   - Sub-Task 1
	 *   - Sub-Task 2
	 *     - Sub-Task 21
	 *     - Sub-Task 22
	 *     - Sub-Task 23
	 * </pre>
	 */
	@Test
	void createTaskTreePipeline_shouldOutputResultsOnAllTreeBranches_whenSeveralTasksAreConcatenated() {
		int parallelism = Math.max((int) Math.floor(ForkJoinPool.getCommonPoolParallelism() / 2f), 2);
		ExecutorService taskExecutor = Executors.newWorkStealingPool(parallelism);
		ExecutorService outputExecutor = Executors.newWorkStealingPool(parallelism);

		TaskTreePipelineConfig<Long> config = TaskTreePipelineConfig.<Long>builder() //
				.rootTask(() -> Flux.interval(Duration.ofMillis(250)).take(10)) //
				.taskExecutor(taskExecutor) //
				.outputExecutor(outputExecutor) //
				.taskTreeNode(TaskTreeLeafNode.<Long, Long, Long>builder() //
						.name("sub-task-1") //
						.task((Long input) -> Mono.just(input + 1)) //
						.build()) //
				.taskTreeNode(TaskTreeIntermediateNode.<Long, Long>builder() //
						.task((Long input) -> Mono.just(input * input)) //
						.taskTreeNode(TaskTreeLeafNode.<Long, String, String>builder() //
								.name("sub-task-21") //
								.task((Long input) -> Mono.just(String.valueOf(input + 1000))) //
								.batchConfig(TaskPipelineBatchConfig.<String, String>builder() //
										.bufferMaxSize(5) //
										.bufferMaxTime(Duration.ofMillis(150)) //
										.batchAggregator(batch -> String.join(",", batch.stream() //
												.toList()))
										.build()) //
								.build()) //
						.taskTreeNode(TaskTreeLeafNode.<Long, String, String>builder() //
								.name("sub-task-22") //
								.task((Long input) -> Mono.just(String.valueOf(input + 2000))) //
								.build()) //
						.taskTreeNode(TaskTreeLeafNode.<Long, String, String>builder() //
								.name("sub-task-23") //
								.task((Long input) -> Mono.just(String.valueOf(input + 3000))) //
								.build()) //
						.build()) //
				.build();

		TaskTreePipeline pipeline = TaskPipelineFactory.create(config);

		// TODO
	}
}
