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
  def outputIsReturnedFromCommand() {
    val baos = new ByteArrayOutputStream()
    System.setOut(new PrintStream(baos))
    val output = Command.execute("git --help")
    assertTrue(output.contains("usage: git"))
  }
  
  @Test
  def stdErrorIsInException() {
    val err = new ByteArrayOutputStream()
    System.setErr(new PrintStream(err))
    
    try {
    	Command.execute("git a")
    	fail()
    }catch {
    	case ex: Command.CommandNonZeroExitCodeException => {
    	  assertTrue(ex.error.contains("is not a git command"))
    	}	  
    	case ex: Exception => fail()
    }
  }
  
}