package main.java.taskpipeline.config;

import java.util.List;

@FunctionalInterface
public interface BatchAggregator<T, R> {

	R aggregate(List<T> batch);
}
