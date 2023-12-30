package main.java.taskpipeline.config;

import java.time.Duration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class TaskPipelineBatchConfig<T, R> {

	@Builder.Default
	int bufferMaxSize = 50;

	@NonNull
	@Builder.Default
	Duration bufferMaxTime = Duration.ofMillis(100);

	@NonNull
	BatchAggregator<T, R> batchAggregator;
}
