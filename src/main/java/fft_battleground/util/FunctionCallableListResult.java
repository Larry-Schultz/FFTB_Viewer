package fft_battleground.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionCallableListResult<T, S> implements Callable<List<S>> {
	private Function<T, S> callingFunction;
	private Collection<T> iteratedObject;

	public FunctionCallableListResult(Collection<T> obj, Function<T, S> callingFunction) {
		this.iteratedObject = obj;
		this.callingFunction = callingFunction;
	}

	@Override
	public List<S> call() throws Exception {
		List<S> result = this.iteratedObject.parallelStream().map(this.callingFunction).collect(Collectors.toList());
		return result;
	}
}