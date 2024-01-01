package taskpipeline.config;

import java.time.Duration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Batching was inspired by
 * https://zone84.tech/programming/creating-batches-with-project-reactor/
 */
@Value
@Builder
public class TaskPipelineBatchConfig<T, U> {

	@Builder.Default
	int bufferMaxSize = 50;

	@NonNull
	@Builder.Default
	Duration bufferMaxTime = Duration.ofMillis(100);

	@NonNull
	BatchAggregator<T, U> batchAggregator;
}
