package org.builder
import java.io.File
import org.builder.versioncontrol.TestRepository
import org.junit.After
import org.junit.Before

trait ReposTest {

  val testDir = new File("testsruntime")
  
  var origin: TestRepository = null
  var repo1: TestRepository = null
  
  @Before
  def setup() {
    origin = setupOrigin()
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
}