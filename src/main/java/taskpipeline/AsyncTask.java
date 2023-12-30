package main.java.taskpipeline;

import org.reactivestreams.Publisher;

@FunctionalInterface
public interface AsyncTask<T> {

	Publisher<T> execute();
}
