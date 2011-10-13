package org.builder.versioncontrol.git.commandline

import org.junit._
import Assert._
import java.io.File
import org.builder.versioncontrol.git.commandline.GitCommand.GitCommandException
import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.Arrays
import org.builder.versioncontrol.git.commandline.GitCommand.CommandNonZeroExitCodeException

class GitCommandTest {
  
  implicit val dir = new File(".")

  @Test
  def wrongCommand() {
    try {
    	GitCommand.execute("wrongCommand")
    	fail()
    } catch {
      case ex: GitCommandException => //assertion true
      case ex: Exception => fail()
    }
  }
  
  @Test 
  def nonZeroExitCode() {
    try {
    	GitCommand.execute("git hello world")
    	fail()
    } catch {
      case ex: CommandNonZeroExitCodeException => //assertion true
      case ex: Exception => fail()
    }
  }
  
  @Test
  def outputIsStreamedToStdOut() {
    val baos = new ByteArrayOutputStream()
    System.setOut(new PrintStream(baos))
    val output = GitCommand.execute("git --help")
    assertTrue(new String(baos.toByteArray()).contains(output))
  }
  
  @Test
  def errorIsStreamedToStdOut() {
    val baos = new ByteArrayOutputStream()
    System.setErr(new PrintStream(baos))
    
    try {
    	GitCommand.execute("git")
    }catch {
    	case ex: CommandNonZeroExitCodeException => 
    	  assertEquals(ex.error, new String(baos.toByteArray()))
    	case ex: Exception => fail()
    }
  }
  
}