package org.builder
import scala.sys.process.ProcessLogger

object MainEnv {
	
	def main(args: Array[String]) {
		val p = scala.sys.process.Process.apply("git")
		p ! ProcessLogger(stdOut, stdErr)
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