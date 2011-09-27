package org.builder.versioncontrol

import org.junit._
import Assert._
import java.io.File

@Test
class TestGit {
	
  val testDir = new File("testsruntime")
  
  var origin: TestRepository = null
  var repo1: TestRepository = null
  var lastCommitOnOrigin: String = null
  
  @Before
  def setup() {
    origin = setupOrigin()
    lastCommitOnOrigin = origin.getLatestRevision("master")
    repo1 = new TestRepository(testDir, "repo1").clone(origin.dir)
  }
  
	private def setupOrigin() = {
		val origin = new TestRepository(testDir, "origin").init()
		val file1 = origin.createNewFile("file1", "test1\n", true)
		origin.commit("added new file")
		origin
	}
	
  @After
  def tearDown() {
    origin.removeRepository
    repo1.removeRepository
  }
  
  @Test
  def lastRevision() {
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
	  val patch = repo1.createPatch()
	  val expectedDiff = 
	  	"--- a/file1\n" + 
	  	"+++ b/file1\n" +
	  	"@@ -1 +1 @@\n" +
	  	"-test1\n" + 
	  	"+test2\n" +
	  	"--"
	  assertTrue(patch.diff.contains(expectedDiff))
	  assertEquals(patch.revision, repo1.getLastCommitAtOriginMaster())
  }
  
  @Test
  def applyPatch() {
  	  val fileName = "file1"
  	  val newFileContent = "test2\n"
	  repo1.editFile(fileName,newFileContent)
	  repo1.commit("changed file1")
	  val patch = repo1.createPatch()

	  val file1Before = origin.getFileAsString(fileName)
	  assertNotSame(newFileContent, file1Before)

	  val patchFile = origin.createNewFile("patch.txt", patch.diff, false)
	  origin.applyPatch(patchFile)
	  
	  val fileContent =  origin.getFileAsString(fileName)
	  assertFalse(file1Before.equals(fileContent))
	  assertEquals(newFileContent, fileContent)
	  assertEquals(repo1.getFileAsString(fileName), fileContent)
  }

}