package org.builder.versioncontrol
import org.junit._
import Assert._
import org.builder.versioncontrol.svn.commandline.SvnInfo

@Test
class TestSvnInfo {
  
  @Test
  def testSvnInfo() {
    val url = "https://svn.trifork.com/svn/trifork/projects/pl/trunk"
    val revision = "22092"
    val input =
      "Path: .\n" +
    		"URL: " +url + "\n" +
    		"Repository Root: https://svn.trifork.com/svn/trifork/projects\n" +
    		"Repository UUID: 931d27ba-d1ef-0310-baf8-e73a6a934ec9\n" +
    		"Revision: " + revision + "\n" +
    		"Node Kind: directory\n" +
    		"Schedule: normal\n" +
    		"Last Changed Author: chr\n" +
    		"Last Changed Rev: 22092\n" +
    		"Last Changed Date: 2011-10-22 20:31:22 +0200 (Sat, 22 Oct 2011)\n" +
    		""
    val info = SvnInfo.parse(input)
    assertEquals(url, info.url)
    assertEquals(revision, info.revision)


  }

}