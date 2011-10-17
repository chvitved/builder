package org.builder.client

import org.junit.Test
import org.junit.Assert._
import org.builder.ReposTest


@Test
class TestCommandLine extends ReposTest{
  
  @Test
  def yesNoTest() {
    val client = new Client(repo1.dir, null)
    assertTrue(client.checkForUntrackedFiles())
    repo1.createNewFile("test.txt", "hello", false)
    assertFalse(client.checkForUntrackedFiles())
  }
  

}