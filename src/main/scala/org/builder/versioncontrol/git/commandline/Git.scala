package org.builder.versioncontrol.git.commandline

import org.builder.versioncontrol.VersionControl
import org.builder.command.Command
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
    Command.execute(command)
  }
  
  override def getLastCommitIdFromOrigin(): String = {
    getLatestRevision("origin/master")
  }
  
  private def getLatestRevision(branch: String): String = {
    val command = "git rev-parse " + branch
    Command.execute(command)
  }
  
  override def createPatch(f: File): Patch = {
    val id = getLastCommitIdFromOrigin()
    val command = String.format("git diff --binary %s ", id)
    Command.execute(command, f.getCanonicalFile())
    Patch(f, id)
    
  }
  
  override def move(src: File, dest: File) {
  	val command = String.format("git mv %s %s", src.getCanonicalFile(), dest.getCanonicalFile())
    Command.execute(command)
  }
  
  override def init() {
    val command = "git init"
    Command.execute(command)
  }
  
  override def clone(directory: String) {
    val command = "git clone " + directory + " ."
    Command.execute(command)
  }
  
  override def checkout(revision: String) {
  	val command = "git checkout " + revision 
    Command.execute(command)
  }
  
  override def add(file: File) {
    checkFilePath(file)
    val command = "git add " + file.getCanonicalPath()
    Command.execute(command)
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
    	Command.execute(command)    	
    }
  }
  
  override def apply(patchFile: File) {
    val command = "git apply " + patchFile.getCanonicalPath() + " --binary"
    Command.execute(command)
  }
  
  override def hasChanges(): Boolean = {
  	val command = "git status "
  	val output = Command.execute(command)
  	!output.contains("nothing to commit")
  }
  
  def untrackedFiles(): Seq[String] = {
  	val command = "git status --porcelain"
  	val output = Command.execute(command)
  	output.split("\n").filter(_.startsWith("??")).map(_.replace("?? ", ""))
  }
  
  private def checkFilePath(file: File): Unit = {
  	
    if (!file.getCanonicalFile().getParentFile().getCanonicalFile().toString().contains(dir.getCanonicalFile().toString())) {
      throw new RuntimeException(String.format("the file %s is not in the directory %s", file, dir))
    }
  }
}