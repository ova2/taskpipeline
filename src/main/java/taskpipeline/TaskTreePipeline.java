package main.java.taskpipeline;

import java.util.Map;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import reactor.core.publisher.Flux;

@Value
public class TaskTreePipeline {

	@Getter(AccessLevel.NONE)
	Map<String, Flux<?>> outputs;

	public <T> Flux<T> getOutput(String key, Class<T> clazz) {
		if (!outputs.containsKey(Objects.requireNonNull(key))) {
			return null;
		}
		return outputs.get(key).cast(clazz);
	}
}
