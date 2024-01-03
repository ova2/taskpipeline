# Non-blocking pipelines for asynchronous task execution
Configurable non-blocking task pipelines enable execution of ongoing tasks in multiple threads. This can significantly enhance performance and efficiency in specific scenarios. The current implementation is based on [Project Reactor](https://projectreactor.io/).

There are two kinds of pipeline:
- `TaskFlowPipeline`. This is a pipeline with an input interface. Several tasks can be emitted into the pipeline via an input interface, then executed in parallel in multiple threads, and batched if necessary. The results of tasks executions are available in form of `Flux` stream. The input and output occur in different threads. The pipeline is highly configurable.
- `TaskTreePipeline`. This is a pipeline with static pre-configured concatenated tasks. Such pre-configured task tree serves as input. The result of task execution of a parent task gets passed as input into all direct child tasks. Every task can be executed in parallel in multiple threads. The results of "leaf" tasks executions can be batched if necessary. The results of tasks executions for task tree branches are available in form of `Flux` streams. Such outputs on task tree branches can occur in different threads. Any upstream broadcasts data to all subscribers along the task tree. The connection to the top upstream source only happens when all subscribers subscribe.

Following sections demonstrate the architecture and usage of mentioned pipelines.

## TaskFlowPipeline
TODO: picture

TODO: usage example

## TaskTreePipeline
TODO: picture

TODO: usage example