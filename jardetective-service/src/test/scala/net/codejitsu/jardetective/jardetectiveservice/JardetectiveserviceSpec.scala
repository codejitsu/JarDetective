package net.codejitsu.jardetective.jardetectiveservice

import collection.mutable.Stack
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class JardetectiveserviceSpec extends FlatSpec with ShouldMatchers {

  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be (2)
    stack.pop() should be (1)
  }
}

