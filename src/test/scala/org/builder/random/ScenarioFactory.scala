package org.builder.random
import org.builder.server.impl.Server
import org.builder.ciserver.CIServerStub
import org.builder.versioncontrol.git.commandline.Git
import java.io.File
import org.builder.versioncontrol.VersionControl
import org.builder.client.Client
import org.builder.server.api.ServerApi
import org.builder.util.FileUtils
import org.builder.versioncontrol.svn.commandline.Svn
import org.builder.versioncontrol.svn.commandline.SvnRepo

class ScenarioFactory(baseDir: File) {
  
	val repo1 = new File(baseDir + File.separator + "repo1")
	val origin = new File(baseDir + File.separator + "origin")
	val buildserverDir = new File(baseDir + File.separator + "buildserver")

	val serverWithGitUrl = "http://localhost:7000"
	val originGitDir = new File(origin, "git");
	val repo1GitDir = new File(repo1, "git");
	val originGitVc = new Git(originGitDir)
	val repo1GitVc = new Git(repo1GitDir)
	
	val buildserverGitVc = new Git(buildserverDir);
    val buildServerStubGit = null //new CIServerStub(buildserverGitVc, originGitDir.getCanonicalPath(), serverWithGitUrl)
    val serverWithGit = new Server().start(buildServerStubGit, 7000)
    
    
    val serverWithSvnUrl = "http://localhost:7001"
    val svnRepoDir = new File(baseDir, "svnrepo")
    FileUtils.createDir(svnRepoDir)
    val svnRepo = SvnRepo.create(svnRepoDir)
    val svnUrl = svnRepo.url + "/svn/trunk/"
    val originSvnDir = new File(origin, "svn");
    val repo1SvnDir = new File(repo1, "svn");
    val originSvnVc = new Svn(originSvnDir, svnRepo)
	val repo1SvnVc = new Svn(repo1SvnDir, svnRepo)
	val buildserverSvnVc = new Svn(buildserverDir, svnRepo)
    
    val buildServerStubSvn = null //new CIServerStub(buildserverSvnVc, svnUrl, serverWithSvnUrl)
    val serverWithSvn = new Server().start(buildServerStubSvn, 7001)
    
    

  def scenarios(): Seq[Scenario] = {
    FileUtils.deleteFile(baseDir)
	FileUtils.createDir(origin)
	FileUtils.createDir(repo1)
	FileUtils.createDir(buildserverDir)
	FileUtils.createDir(svnRepoDir)
	SvnRepo.create(svnRepoDir) //TODO it is a hack removing the repo and setting it up again like this
	FileUtils.createDir(originGitDir)
    FileUtils.createDir(repo1GitDir)
    FileUtils.createDir(originSvnDir)
    FileUtils.createDir(repo1SvnDir)

    val clientWithGit = new Client(repo1GitVc, new ServerApi(serverWithGitUrl))
    val gitScenario = Scenario(originGitVc, repo1GitVc, clientWithGit, buildserverDir)
    
    val clientWithSvn = new Client(repo1SvnVc, new ServerApi(serverWithSvnUrl))
	val svnScenario = Scenario(originSvnVc, repo1SvnVc, clientWithSvn, buildserverDir)
    
    List(gitScenario, svnScenario)
  }
}

case class Scenario(
  val origin : VersionControl,
  val repo1 : VersionControl,
  val client: Client,
  val buildserverDir: File
  )