package org.builder.random
import scala.sys.process.ProcessLogger
import org.scalacheck.Gen

object MainEnv {
	
	def main(args: Array[String]) {
		//val p = scala.sys.process.Process.apply("git")
		//p ! ProcessLogger(stdOut, stdErr)
		val out = Generators.genTextBytes(5)
		println(out.sample.get.size)
	}
	
	def stdOut(str: String) = {
      System.out.println(str)
      System.out.append(str + "\n")
    }

	def stdErr(str: String) = {
      System.err.println(str)
      System.err.append(str + "\n")
    }
}