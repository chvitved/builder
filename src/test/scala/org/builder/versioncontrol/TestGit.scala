package org.builder.versioncontrol

import java.io.File
import org.apache.commons.io.FileUtils
import org.builder.ReposTest
import org.junit.Assert._
import org.junit._
import org.builder.command.Command
import org.builder.command.Command.CommandNonZeroExitCodeException

@Test
class TestGit extends ReposTest{
	
  @Test
  @Ignore
  def lastRevision() {
  	//val lastCommitOnOrigin = origin.getLastCommit()
    //assertEquals(lastCommitOnOrigin, repo1.getLastCommitAtOrigin())
  }
  
  @Test
  def hasChanges() {
    assertFalse(repo1.hasChanges())
    repo1.createNewFile("newFile.txt", "i am new", true)
    assertTrue(repo1.hasChanges())
  }
  
  @Test
  def untrackedFiles() {
    assertTrue(repo1.untrackedFiles.isEmpty)
    repo1.createNewFile("newFile.txt", "i am new", true)
    assertTrue(repo1.untrackedFiles.isEmpty)
    val untrackedFile = "newUntrackedFile.txt"
    repo1.createNewFile(untrackedFile, "i am untracked", false)
    assertEquals(List(untrackedFile), repo1.untrackedFiles)
    val untrackedFile2 = "newUntrackedFile2.txt"
    repo1.createNewFile(untrackedFile2, "i am also untracked", false)
    assertEquals(List(untrackedFile, untrackedFile2), repo1.untrackedFiles)
  }
  
  
  @Test
  def createPatch() {
	  repo1.editFile("file1", "test2\n")
	  repo1.commit("changed file1")
	  val patchFile = new File(testDir, "patch")
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
	  assertEquals(patch.revision, repo1.getLastCommitAtOrigin())
  }
  
  @Test
  def applyPatch() {
  	  val fileName = "file1"
  	  val newFileContent = "test2\n"
	  repo1.editFile(fileName,newFileContent)
	  repo1.commit("changed file1")
	  val patchFile = new File(testDir, "patch")
	  val patch = repo1.createPatch(patchFile)
	  assertEquals(patch.revision, repo1.getLastCommitAtOrigin())
	  
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