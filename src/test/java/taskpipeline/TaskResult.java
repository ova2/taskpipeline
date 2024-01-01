package taskpipeline;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskResult {

	String name;

	@Builder.Default
	List<Integer> result = List.of();
}
