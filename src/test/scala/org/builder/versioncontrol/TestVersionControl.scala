package org.builder.versioncontrol

import java.io.File
import org.apache.commons.io.FileUtils
import org.builder.ReposTest
import org.junit.Assert._
import org.junit._
import org.builder.command.Command
import org.builder.command.Command.CommandNonZeroExitCodeException
import java.io.FileWriter
import org.builder.ForAll

@Test
class TestVersionControl extends ReposTest{
	
  @Test
  @Ignore
  def lastRevision() {
    ForAll.forAll((origin, repo1) => {
      val lastCommitOnOrigin = origin.getLastCommitIdFromOrigin()
      assertEquals(lastCommitOnOrigin, repo1.getLastCommitAtOrigin())
    })
  }
  
  @Test
  def hasNewFilesChanges() {
    ForAll.forAll((origin, repo1) => {
      assertFalse(repo1.hasChanges())
      repo1.createNewFile("newFile.txt", "i am new", true)
      assertTrue(repo1.hasChanges())
    })
  }
  
  @Test
  def hasChangedFiles() {
    ForAll.forAll((origin, repo1) => {
	    assertFalse(repo1.hasChanges())
	    repo1.createNewFile("newFile.txt", "i am new", true)
	    assertTrue(repo1.hasChanges())
    })
  }
  
  @Test
  def untrackedFiles() {
    ForAll.forAll((origin, repo1) => {
	    assertTrue(repo1.untrackedFiles.isEmpty)
	    repo1.createNewFile("newFile.txt", "i am new", true)
	    assertTrue(repo1.untrackedFiles.isEmpty)
	    val untrackedFile = "newUntrackedFile.txt"
	    repo1.createNewFile(untrackedFile, "i am untracked", false)
	    assertEquals(List(untrackedFile), repo1.untrackedFiles)
	    val untrackedFile2 = "newUntrackedFile2.txt"
	    repo1.createNewFile(untrackedFile2, "i am also untracked", false)
	    assertEquals(List(untrackedFile, untrackedFile2), repo1.untrackedFiles)
    })
  }
  
  @Test
  def applyPatch() {
    ForAll.forAll((origin, repo1) => {
  	  val fileName = "file1"
  	  val newFileContent = "test2\n"
	  repo1.editFile(fileName,newFileContent)
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
    })
  }
  
  @Test
  def applyWrongPatch() {
    ForAll.forAll((origin, repo1) => {
	  	val patchFile = new File("patch.txt")
	  	new FileWriter(patchFile).write("wrong patch")
	  	try {
	  		origin.applyPatch(patchFile)
	  		fail()
	  	} catch {
	  		case ex: Exception => 
	  	}
    })
  }

}