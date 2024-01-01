package taskpipeline;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Value
@Builder
@Slf4j
public class ConcreteTaskSupplier implements TaskSupplier<TaskResult> {

	String name;

	@Override
	public Mono<TaskResult> execute() {
		// execute a long-running task (just for demo purpose)
		List<Integer> result = new ArrayList<>();
		int size = new SecureRandom().nextInt(100000, 5000000);
		for (int i = 0; i < size; i++) {
			result.add(new Random().nextInt(100));
		}

		log.debug("AsyncTask with name {} and result size {} has been executed in thread {}", //
				name, result.size(), Thread.currentThread().getId());

		return Mono.just(TaskResult.builder() //
				.name(name) //
				.result(result) //
				.build());
	}
}
