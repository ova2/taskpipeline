package test.java;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AsyncTaskResult {

	String name;

	@Builder.Default
	List<Integer> result = List.of();
}
