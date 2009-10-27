package jboss.cloud


import java.util.concurrent.{Future, Executors, Callable}

/**
 * Sample implementation of a Parallel Future based algebra.  
 * @author Michael Neale
 */

object FutureProcess {
      val executor = Executors.newCachedThreadPool
      implicit def fromFuture[T](future: Future[T]) : T = future.get
      def future[T](lambda: => T) : Future[T] = executor.submit(new Callable[T] { def call = lambda })
}