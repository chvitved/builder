package org.builder.versioncontrol.git.commandline

import org.builder.versioncontrol.VersionControl
import org.builder.command.Command
import scala.sys.process.Process
import java.io.File
import org.builder.versioncontrol.Patch
import org.apache.commons.io.FileUtils
import scala.sys.process.Process
import java.io.ByteArrayOutputStream
import org.builder.versioncontrol.VCType
import org.apache.log4j.Logger
	
class Git(directory: File) extends VersionControl{

  val logger = Logger.getLogger(classOf[Git])
  
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
    Command.execute(command).replace("\n", "")
  }
  
  override def createPatch(f: File): Patch = {
    val id = getLastCommitIdFromOrigin()
    val command = String.format("git diff --binary %s ", id)
    Command.execute(command, f.getCanonicalFile())
    logger.info(status)
    Patch(f, id)
  }
  
  def originUrl = {
    val command = "git config --get remote.origin.url"
    Command.execute(command).replace("\n", "")
  }
  
  def vcType = VCType.git
  
  override def move(src: File, dest: File) {
    if (!(src.isDirectory() && src.list().isEmpty)) {
      val command = String.format("git mv %s %s", src.getCanonicalFile(), dest.getCanonicalFile())
      Command.execute(command)
    }
  }
  
  override def init() {
    val command = "git init"
    Command.execute(command)
  }
  
  override def clone(directory: File) {
    clone(directory.getCanonicalPath())
  }
  
  override def clone(url: String) {
    val command = "git clone " + url + " ."
    Command.execute(command)
  }
  
  def cloneAndCheckout(url: String, revision: String) {
    clone(url)
    checkout(revision)
  }
  
  override def checkout(revision: String) {
  	val command = "git checkout " + revision 
    Command.execute(command)
  }
  
  override def add(file: File) {
    if (!file.isDirectory()) {
    	checkFilePath(file)
    	val command = "git add " + file.getCanonicalPath()
    	Command.execute(command)
    }
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
  
  private def status : String = {
    val command = "git status --porcelain"
  	Command.execute(command)
  }
  
  override def hasChanges(): Boolean = {
  	val st = status
  	!st.isEmpty()
  }
  
  def untrackedFiles(): Seq[String] = {
    val st = status
  	val res = st.split("\n").filter(_.startsWith("??")).map(_.replace("?? ", ""))
  	if (!res.isEmpty) {
  	  logger.info(st)
  	}
    res
  }
  
  private def checkFilePath(file: File): Unit = {
  	
    if (!file.getCanonicalFile().getParentFile().getCanonicalFile().toString().contains(dir.getCanonicalFile().toString())) {
      throw new RuntimeException(String.format("the file %s is not in the directory %s", file, dir))
    }
  }
}