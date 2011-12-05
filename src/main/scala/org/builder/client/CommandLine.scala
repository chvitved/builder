package org.builder.client
import java.util.Scanner
import org.apache.log4j.Logger

class CommandLine {
  
  val logger = Logger.getLogger(classOf[CommandLine])
  
  def yesNo(output: String) : Boolean = {
    logger.info(output)
    logger.info("answer y/n")
    val scanner = new Scanner(System.in)
    var res = false
    var gotAnswer = false
    while(!gotAnswer) {
    	getAnswer(scanner) match {
    	  case Ok(result) => {
    		  res = result
    		  gotAnswer = true
    		}
    	  case NotUnderstood() => {
    	    gotAnswer = false
    	    logger.info("Could not understand your answer try again")
    	  }
    	}
    }
    res
  }
  
  abstract sealed case class Answer
  case class Ok(res: Boolean) extends Answer
  case class NotUnderstood() extends Answer
  
  private def getAnswer(scanner: Scanner): Answer = {
    val answer = scanner.nextLine().trim;
    if (answer == "y" || answer == "Y") {
      Ok(true)
    } else if (answer == "n" || answer == "N") {
      Ok(false)
    } else {
      NotUnderstood() 
    }
  }

}