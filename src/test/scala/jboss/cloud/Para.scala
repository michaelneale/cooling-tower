package jboss.cloud


import java.util.concurrent.{Future, Executors, Callable}

/**
 * Sample implementation of a Parallel Future based algebra.  
 * @author Michael Neale
 */

object Para {
      implicit def fromFuture[T](future: Future[T]) : T = future.get
      val executor = Executors.newFixedThreadPool (5)
      def spawn[T](lambda: => T) : Future[T] = executor.submit(new Callable[T] { def call = lambda })
}