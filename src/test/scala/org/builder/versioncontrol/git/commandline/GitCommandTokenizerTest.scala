package org.builder.versioncontrol.git.commandline

import org.junit._
import Assert._
import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.Arrays

class GitCommandTokenizerTest {

  @Test
  def tokenizeNoQuotes() {
    val command = "command with 4 params"
    val result = GitCommand.tokenizeCommand(command)
    assertCommand(List("command", "with", "4", "params"), result)
  }
  
  @Test
  def tokenizeSingleCommand() {
    val command = "command"
    val result = GitCommand.tokenizeCommand(command)
    assertCommand(List("command"), result)
  }
  
  @Test
  def tokenize1Quotes() {
    val command = """git commit -m "long commit message with many words""""
    val result = GitCommand.tokenizeCommand(command)
    assertCommand(List("git", "commit", "-m", "long commit message with many words"), result)
  }
  
  @Test
  def tokenizeOnlyAQuote() {
    val command = """"long command with many words""""
    val result = GitCommand.tokenizeCommand(command)
    assertCommand(List("long command with many words"), result)
  }
  
  @Test
  def tokenizingCommandWith2QuotedParams() {
    val command = "git commit -m \"lang besked med mellemrum\" param5 param6 \"endnu en lang besked\""
    val result = GitCommand.tokenizeCommand(command).toList
    assertCommand(List("git", "commit", "-m", "lang besked med mellemrum", "param5", "param6", "endnu en lang besked"), result)
  }
  
  def assertCommand(expected: Seq[String], result: Seq[String]) {
    assertEquals(expected.toList, result.toList)
  }
  
}