package org.builder.command

import java.io.File
import scala.sys.process.{Process, ProcessLogger}
import java.util.regex.Pattern
import java.io.InputStream
import scala.sys.process.ProcessIO
import java.io.OutputStream
import java.io.ByteArrayInputStream
import org.apache.commons.io.IOUtils

object Command {
	
  def execute(command: String, file: File)( implicit dir: File) {
   println(command)
   val pb = Process(command, dir) #> file
   val exitValue = pb !	 

   if (exitValue != 0) {
    	throw new CommandNonZeroExitCodeException(command, exitValue, "output was send to a patch file", "", dir)
   }
  }
    
  def execute(command: String)( implicit dir: File) : String = {
    val tokenizedCommand: Seq[String] = tokenizeCommand(command)
    val pb = Process(tokenizedCommand, dir) 
    
    val output = new StringBuilder()
    val error = new StringBuilder()
    
    def stdOut(str: String) = {
   		System.out.println(str)
   		output.append(str + "\n")
    }
    def stdErr(str: String) = {
      System.err.println(str)
      error.append(str + "\n")
    }
   	val exitValue = try {
    	 println(command)
    	 pb ! ProcessLogger(stdOut, stdErr)
    } catch {
      case ex: Exception => throw new CommandException(command, ex, dir)
    }
    if (exitValue != 0) {
    	throw new CommandNonZeroExitCodeException(command, exitValue, output.toString(), error.toString(), dir)
    } else {
    	output.toString
    }
  }
  
  
  
  def tokenizeCommand(command: String) = {
    val tokens =  command.split("\\\"") //find strings in quotes
    val  tokenizedCommand = 
      for (index <- 0 until tokens.length) 
        yield
    	  if (index % 2 == 1) { //string in quotes
    	    Array((tokens(index)))
    	  } else { //string not in quotes
    	    tokens(index).split("\\s") //strings not in quotes are split by whitespaces
    	  }
     tokenizedCommand.flatten.filter(_.trim() != "")
  }
  
  class CommandException(command: String, ex: Exception, dir: File) 
  		extends Exception("Is git on the path? " + "Could not invoke git command '" + command + "' in directory " + dir + ". " + ex.getMessage(), ex)
  
  class CommandNonZeroExitCodeException(command: String, val exitCode: Int, val output: String, val error: String, val dir: File) 
  		extends Exception("git command exited with exitcode " + exitCode + "\ncommand: " + command + "\ndirecotry: " + dir + "\nstdout: " + output + "\nstderror: " + error)
  
}