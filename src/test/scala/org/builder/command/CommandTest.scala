package org.builder.command

import org.junit._
import Assert._

import java.io.File
import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.Arrays

class CommandTest {
  
  implicit val dir = new File(".")

  @Test
  def wrongCommand() {
    try {
    	Command.execute("wrongCommand")
    	fail()
    } catch {
      case ex: Command.CommandException => //assertion true
      case ex: Exception => fail()
    }
  }
  
  @Test 
  def nonZeroExitCode() {
    try {
    	Command.execute("git hello world")
    	fail()
    } catch {
      case ex: Command.CommandNonZeroExitCodeException => //assertion true
      case ex: Exception => fail()
    }
  }
  
  @Test
  def outputIsStreamedToStdOut() {
    val baos = new ByteArrayOutputStream()
    System.setOut(new PrintStream(baos))
    val output = Command.execute("git --help")
    assertTrue(new String(baos.toByteArray()).contains(output))
  }
  
  @Test
  def errorIsStreamedToStdOut() {
    val baos = new ByteArrayOutputStream()
    System.setErr(new PrintStream(baos))
    
    try {
    	Command.execute("git")
    }catch {
    	case ex: Command.CommandNonZeroExitCodeException => 
    	  assertEquals(ex.error, new String(baos.toByteArray()))
    	case ex: Exception => fail()
    }
  }
  
}