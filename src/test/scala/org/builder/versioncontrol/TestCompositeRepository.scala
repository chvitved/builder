package org.builder.versioncontrol
import java.io.File
import org.builder.util.FileUtils
import org.builder.versioncontrol.git.commandline.Git
import org.builder.versioncontrol.svn.commandline.Svn
import org.builder.versioncontrol.svn.commandline.SvnRepo
import org.junit.Assert._
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.io.FileWriter
class TestCompositeRepository(myDir: File) extends TestRepository{

	val gitDir = new File(myDir, "git") 
	FileUtils.createDir(gitDir)
	
	val svnDir = new File(myDir, "svn") 
	FileUtils.createDir(svnDir)
	
	val git = new Git(gitDir)

	//val svnRepoDir = new File(dir.getParent(), "svnrepo")
	//FileUtils.createDir(svnRepoDir)
	
	//val svn = new Svn(svnDir, SvnRepo.create(svnRepoDir))

	val repos = Seq(new TestRepositoryImpl(git))

	private def all[T](m: TestRepositoryImpl => T) = {
		val results = for(repo <- repos) yield m(repo)
		val firstResult = results.head
		for(r <- results.tail) assertEquals(firstResult, r)
		firstResult
	}
	
	def dir = throw new UnsupportedOperationException()
	
	def init() {
		all(_.init())
	}
	
	def hasChanges() = all(_.hasChanges())
	
	def untrackedFiles() = all(_.untrackedFiles())
	
	def clone(r: TestRepository) {
	  assertTrue(r.isInstanceOf[TestCompositeRepository])
	  val repo = r.asInstanceOf[TestCompositeRepository]
	  val zippedRepos = this.repos.zip(repo.repos)
	  for((myRepo,repoToClone) <- zippedRepos) {
	    assertEquals(myRepo.vc.getClass, repoToClone.vc.getClass)
	    myRepo.clone(repoToClone)
	  }
	}
	
	def commit(msg: String) {
	  all(_.commit(msg))
	}
	
	def createPatch(file: File): Patch = {
	  all(_.createPatch(file))
	}
	
	def applyPatch(f: File) {
		all(_.applyPatch(f))
	}
	
	def getLastCommitAtOrigin(): String = {
	  all(_.getLastCommitAtOrigin())
	}
	
	def getFileAsString(fileName: String) = {
		all(r => IOUtils.toString(new FileInputStream(new File(r.dir, fileName))))
	}

	def createNewFile(name: String, content: String, addToVC: Boolean){
	  all(_.createNewFile(name, content, addToVC))
	}
	
	def editFile(name: String, content: String) {
	  all(_.editFile(name, content))
	}

}