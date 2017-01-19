package common;

import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

public class Util {
	public static class SimpleHandler<A> implements CompletionHandler<A, Void> {
		private Consumer<A> onComplete;
		private Consumer<Throwable> onFail;

		public SimpleHandler(Consumer<A> complete, Consumer<Throwable> fail) {
			this.onComplete = complete;
			this.onFail = fail;
		}

		@Override
		public void completed(A result, Void attachment) {
			onComplete.accept(result);
		}

		@Override
		public void failed(Throwable exc, Void attachment) {
			onFail.accept(exc);
		}
	}
}
