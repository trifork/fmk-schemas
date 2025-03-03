package dk.medicinkortet.notproduktion.validateWikiExamples.xmlvalidator;

import java.util.concurrent.*;

/**
 * This is the final implementation of Memoizer from Java Concurrency in Practice.
 * <p/>
 * A Memoizer is a Generic class that encapsulates the process of creating a value, 
 * and remembering the value (memoizing it) so that subsequent calls to get the value 
 * will return the memoized value, and not call the Factory method to create it.
 * <p/>
 * A Memoizer encapsulates the process of "getOrCreate()" which is highly useful when 
 * you are getting a resource that does not change by a key (think cache) and which 
 * is generally considered to be expensive to create.
 * 
 * @author Brian Goetz and Tim Peierls
 */
public class Memoizer<A, V> implements Computable<A, V> {
	private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();
	private final Computable<A, V> c;

	public Memoizer(Computable<A, V> c) {
		this.c = c;
	}

	public V compute(final A arg) throws InterruptedException {
		while (true) {
			Future<V> f = cache.get(arg);
			if (f == null) {
				Callable<V> eval = new Callable<V>() {
					public V call() throws InterruptedException {
						return c.compute(arg);
					}
				};
				FutureTask<V> ft = new FutureTask<V>(eval);
				f = cache.putIfAbsent(arg, ft);
				if (f == null) {
					f = ft;
					ft.run();
				}
			}
			try {
				return f.get();
			} catch (CancellationException e) {
				cache.remove(arg, f);
			} catch (ExecutionException e) {
				Throwable t = e.getCause();
				if (t instanceof RuntimeException)
		        	throw (RuntimeException) t;
		        else if (t instanceof Error)
		            throw (Error) t;
		        else
		            throw new RuntimeException("Expected unchecked exception", t);
			}
		}
	}

	public void invalidate() {
		cache.clear();
	}

}
