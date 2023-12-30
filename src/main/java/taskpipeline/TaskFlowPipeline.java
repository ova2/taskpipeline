package main.java.taskpipeline;

import lombok.Value;
import lombok.experimental.NonFinal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Value
@NonFinal
public class TaskFlowPipeline<T, R> {

	Sinks.Many<AsyncTask<T>> input;

	Flux<R> output;
}
