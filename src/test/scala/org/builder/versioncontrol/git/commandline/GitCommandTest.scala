package org.builder.versioncontrol.git.commandline

import org.junit._
import Assert._
import java.io.File
import org.builder.versioncontrol.git.commandline.GitCommand.GitCommandException
import org.builder.versioncontrol.git.commandline.GitCommand.GitCommandNonZeroExitCodeException
import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.Arrays

class GitCommandTest {
  
  implicit val dir = new File(".")

  @Test
  def exception() {
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
      case ex: GitCommandNonZeroExitCodeException => //assertion true
      case ex: Exception => fail()
    }
  }
  
  @Test
  def outputIsStreamedToStdOut() {
    val baos = new ByteArrayOutputStream()
    System.setOut(new PrintStream(baos))
    val output = GitCommand.execute("git --help")
    assertEquals(output, new String(baos.toByteArray()))
  }
  
  @Test
  def errorIsStreamedToStdOut() {
    val baos = new ByteArrayOutputStream()
    System.setErr(new PrintStream(baos))
    
    try {
    	GitCommand.execute("git")
    }catch {
    	case ex: GitCommandNonZeroExitCodeException => 
    	  assertEquals(ex.error, new String(baos.toByteArray()))
    	case ex: Exception => fail()
    }
  }
  
}