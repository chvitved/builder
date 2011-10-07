package org.builder.versioncontrol.git.commandline

import java.io.File
import scala.sys.process.{Process, ProcessLogger}
import java.util.regex.Pattern

object GitCommand {
  
  def execute(command: String)( implicit dir: File) : String = {
    
    val tokenizedCommand: Seq[String] = tokenizeCommand(command)
    
    val pb = Process(tokenizedCommand, dir)
    
    val output = new StringBuilder()
    val error = new StringBuilder()
    
    def stdOut(str: String) = {
      System.out.println(str)
      val bytes = str.getBytes()
      str.getBytes()
      output.append(new String(bytes, "UTF-8") + "\n")
    }
    def stdErr(str: String) = {
      System.err.println(str)
      val bytes = str.getBytes()
      str.getBytes()
      output.append(new String(bytes, "UTF-8") + "\n")
    }
    
    val exitCode = try {
    	 println(command)
    	 pb ! ProcessLogger(stdOut, stdErr)
    } catch {
      case ex: Exception => throw new GitCommandException(command, ex, dir)
    }
    
    if (exitCode != 0) {
    	throw new GitCommandNonZeroExitCodeException(command, exitCode, output.toString(), error.toString(), dir)
    } else {
   		output.toString()
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
  
  class GitCommandException(command: String, ex: Exception, dir: File) 
  		extends Exception("Is git on the path? " + "Could not invoke git command '" + command + "' in directory " + dir + ". " + ex.getMessage(), ex)
  
  class GitCommandNonZeroExitCodeException(command: String, val exitCode: Int, val output: String, val error: String, val dir: File) 
  		extends Exception("git command exited with exitcode " + exitCode + "\ncommand: " + command + "\ndirecotry: " + dir + "\nstdout: " + output + "\nstderror: " + error)
  
}