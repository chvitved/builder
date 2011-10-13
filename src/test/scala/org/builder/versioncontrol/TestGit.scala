package org.builder.versioncontrol

import org.junit._
import Assert._
import java.io.File
import org.apache.commons.io.FileUtils
import org.builder.versioncontrol.git.commandline.GitCommand.CommandNonZeroExitCodeException
import org.builder.ReposTest

@Test
class TestGit extends ReposTest{
	
  @Test
  def lastRevision() {
  	val lastCommitOnOrigin = origin.getLatestRevision("master")
    assertEquals(lastCommitOnOrigin, repo1.getLastCommitAtOriginMaster())
  }
  
  @Test
  def hasChanges() {
    assertFalse(repo1.hasChanges())
    repo1.createNewFile("newFile.txt", "i am new", true)
    assertTrue(repo1.hasChanges())
  }
  
  
  @Test
  def createPatch() {
	  repo1.editFile("file1", "test2\n")
	  repo1.commit("changed file1")
	  val patchFile = new File(repo1.dir, "patch")
	  val patch = repo1.createPatch(patchFile)
	  val expectedDiff = 
	  	"diff --git a/file1 b/file1\n" + 
	  	"index a5bce3f..180cf83 100644\n" + 
	  	"--- a/file1\n" + 
	  	"+++ b/file1\n" + 
	  	"@@ -1 +1 @@\n" + 
	  	"-test1\n" +
	  	"+test2\n";
	  assertTrue(FileUtils.readFileToString(patch.diffFile).contains(expectedDiff))
	  assertEquals(patch.revision, repo1.getLastCommitAtOriginMaster())
  }
  
  @Test
  def applyPatch() {
  	  val fileName = "file1"
  	  val newFileContent = "test2\n"
	  repo1.editFile(fileName,newFileContent)
	  repo1.commit("changed file1")
	  val patchFile = new File(repo1.dir, "patch")
	  val patch = repo1.createPatch(patchFile)

	  val file1Before = origin.getFileAsString(fileName)
	  assertNotSame(newFileContent, file1Before)
	  
	  origin.applyPatch(patchFile)
	  
	  val fileContent =  origin.getFileAsString(fileName)
	  assertFalse(file1Before.equals(fileContent))
	  assertEquals(newFileContent, fileContent)
	  assertEquals(repo1.getFileAsString(fileName), fileContent)
  }
  
  @Test
  def applyWrongPatch() {
  	val patchFile = repo1.createNewFile("patch.txt", "wrong patch", false)
  	try {
  		origin.applyPatch(patchFile)
  	} catch {
  		case ex: CommandNonZeroExitCodeException =>
  		case ex: Exception => fail()
  	}
  	
  }

}