package taskpipeline.config;

import java.util.List;

@FunctionalInterface
public interface BatchAggregator<T, U> {

	U aggregate(List<T> batch);
}
