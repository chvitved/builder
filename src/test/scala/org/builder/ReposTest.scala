package org.builder
import java.io.File
import org.builder.versioncontrol.TestRepositoryImpl
import org.junit.After
import org.junit.Before
import org.builder.util.FileUtils
import org.builder.versioncontrol.TestRepository
import org.builder.versioncontrol.TestCompositeRepository

trait ReposTest {

  val testDir = new File("testsruntime")
  val originDir = new File(testDir, "origin")
  val repo1Dir = new File(testDir, "repo1")
  var origin: TestRepository = null
  var repo1: TestRepository = null
  
  @Before
  def setup() {
    origin = setupOrigin()
    repo1 = new TestCompositeRepository(repo1Dir)
    repo1.clone(origin)
  }
  
	private def setupOrigin() = {
		val origin = new TestCompositeRepository(originDir)
		origin.init()
		val file1 = origin.createNewFile("file1", "test1\n", true)
		origin.commit("added new file")
		origin
	}
	
  @After
  def tearDown() {
    FileUtils.deleteFile(testDir)
  }
}