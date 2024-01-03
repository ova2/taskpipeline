package taskpipeline.config;

import java.util.List;

/**
 * Specifies an aggregator function for batched outputs.
 */
@FunctionalInterface
public interface BatchAggregator<T, U> {

	U aggregate(List<T> batch);
}
