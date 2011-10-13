package org.builder.versioncontrol.git.commandline

import org.builder.versioncontrol.VersionControl
import scala.sys.process.Process
import java.io.File
import org.builder.versioncontrol.Patch
import org.apache.commons.io.FileUtils
import scala.sys.process.Process
import java.io.ByteArrayOutputStream
	
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
  
  override def createPatch(f: File): Patch = {
    val id = getLastCommitIdAtOriginMaster()
    val command = String.format("git diff --binary %s ", id)
    GitCommand.execute(command, f)
    Patch(f, id)
    
  }
  
  override def move(src: File, dest: File) {
  	val command = String.format("git mv %s %s", src, dest)
    GitCommand.execute(command)
  }
  
  override def init() {
    val command = "git init"
    GitCommand.execute(command)
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
  
  override def remove(file: File) {
    checkFilePath(file)
    
    val command = 
    	if (file.isDirectory()) {
    		if (!file.list().isEmpty) {
    			"git rm -r " + file.getCanonicalPath()
    		} else null
    	} else {
    		"git rm " + file.getCanonicalPath()
    	}
    if (command != null) {
    	GitCommand.execute(command)    	
    }
  }
  
  override def apply(patchFile: File) {
    val command = "git apply " + patchFile.getCanonicalPath() + " --binary"
    GitCommand.execute(command)
  }
  
  override def hasChanges(): Boolean = {
  	val command = "git status "
  	val output = GitCommand.execute(command)
  	!output.contains("nothing to commit")
  }
  
  def untrackedFiles(): Seq[String] = {
  	val command = "git status --porcelain"
  	val output = GitCommand.execute(command)
  	output.split("\n").filter(_.startsWith("??")).map(_.replace("?? ", ""))
  }
  
  private def checkFilePath(file: java.io.File): Unit = {
  	
    if (!file.getCanonicalFile().getParentFile().getCanonicalFile().toString().contains(dir.getCanonicalFile().toString())) {
      throw new RuntimeException(String.format("the file %s is not in the directory %s", file, dir))
    }
  }
}