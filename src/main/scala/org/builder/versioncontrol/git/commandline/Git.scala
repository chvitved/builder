package org.builder.versioncontrol.git.commandline

import org.builder.versioncontrol.VersionControl
import scala.sys.process.Process
import java.io.File
import org.builder.versioncontrol.Patch

class Git(directory: File) extends VersionControl{

  implicit val dir = directory
  
  override def commit(message: String) {
    val command = String.format("""git commit -m "%s"""", message)
    GitCommand.execute(command)
  }
  
  override def getLastCommitIdAtOriginMaster(): String = {
    getLatestRevision("origin/master")
  }
  
  def getLatestRevision(branch: String): String = {
    val command = "git rev-parse " + branch
    GitCommand.execute(command)
  }
  
  override def createPatch(): Patch = {
    val id = getLastCommitIdAtOriginMaster()
    val command = String.format("git format-patch %s --stdout", id)
    val diff = GitCommand.execute(command)
    Patch(diff, id)
  }
  
  override def init() {
    val command = "git init"
    val output = GitCommand.execute(command)
  }
  
  override def clone(directory: String) {
    val command = "git clone " + directory + " ."
    GitCommand.execute(command)
  }
  
  override def checkout(revision: String) {
  	val command = "git checkout " + revision 
    GitCommand.execute(command)
  }
  
  override def add(file: File) {
    checkFilePath(file)
    val command = "git add " + file.getCanonicalPath()
    GitCommand.execute(command)
  }
  
  override def apply(patchFile: File) {
  	checkFilePath(patchFile)
    val command = "git apply " + patchFile.getName()
    GitCommand.execute(command)
  }
  
  private def checkFilePath(file: java.io.File): Unit = {
    if (!file.getParentFile().getCanonicalFile().toString().contains(dir.getCanonicalFile().toString())) {
      throw new RuntimeException(String.format("the file %s is not in the directory %s", file, dir))
    }
  }
}