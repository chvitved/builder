package org.builder
import java.io.File

import org.builder.util.FileUtils
import org.builder.versioncontrol.git.commandline.Git
import org.builder.versioncontrol.svn.commandline.Svn
import org.builder.versioncontrol.svn.commandline.SvnRepo
import org.builder.versioncontrol.TestRepository
import org.junit.After
import org.junit.Before
import org.junit.Ignore

trait ReposTest {

  val testDir = new File("testsruntime")
  FileUtils.deleteFile(testDir)
  
  val svnRepoDir = new File(testDir, "svnrepo")
  FileUtils.createDir(svnRepoDir)
  val svnRepo = SvnRepo.create(svnRepoDir)
  
  val originDir = new File(testDir, "origin")
  val originGitDir = new File(originDir, "git")
  val originSvnDir = new File(originDir, "svn")
  FileUtils.createDir(originGitDir)
  FileUtils.createDir(originSvnDir)
  
  val repo1Dir = new File(testDir, "repo1")
  val repo1GitDir = new File(repo1Dir, "git")
  val repo1SvnDir = new File(repo1Dir, "svn")
  FileUtils.createDir(repo1GitDir)
  FileUtils.createDir(repo1SvnDir)
  
  val originGit = new TestRepository(new Git(originGitDir))
  val originSvn = new TestRepository(new Svn(originSvnDir, svnRepo))
  
  val repo1Git = new TestRepository(new Git(repo1GitDir))
  val repo1Svn = new TestRepository(new Svn(repo1SvnDir, svnRepo))

  val origins = List(originGit, originSvn)
  val repo1s = List(repo1Git, repo1Svn)
  
  implicit val repos: Seq[(TestRepository, TestRepository)] = origins.zip(repo1s)
  
  
  @Before
  def setup() {
     for((origin, repo1) <- repos) {
		origin.init()
		val file1 = origin.createNewFile("file1", "test1\n", true)
		origin.commit("added new file")
		repo1.clone(origin)
	  }
  }
  
	
  @After
  def tearDown() {
    FileUtils.deleteFile(testDir)
  }
}