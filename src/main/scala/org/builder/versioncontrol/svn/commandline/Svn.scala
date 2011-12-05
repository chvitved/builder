package org.builder.versioncontrol.svn.commandline

import java.io.File
import org.builder.versioncontrol.VersionControl
import org.builder.command.Command
import org.builder.versioncontrol.Patch
import org.builder.versioncontrol.svn.patcher._
import org.builder.versioncontrol.VCType
import org.apache.log4j.Logger

class Svn(directory: File, repo: SvnRepo) extends VersionControl{
  
  val logger = Logger.getLogger(classOf[Svn])
  
  def this(directory: File) {
    this(directory, null)
  }

  implicit val dir = directory
  
  override def commit(message: String) {
    val command = String.format("""svn commit -m "%s"""", message)
    Command.execute(command)
  }
  
  private def info(): SvnInfo = {
    val command = String.format("svn info")
    SvnInfo.parse(Command.execute(command))
  }
  
  def originUrl = info().url
  
  def vcType = VCType.svn
  
  override def getLastCommitIdFromOrigin(): String = {
    info.revision
  }
  
  override def createPatch(f: File): Patch = {
    val svnInfo = info
    val id = svnInfo.revision
    CreatePatch.createPatchFromStatus(status, dir, f)
    Patch(f, id)
    
  }
  
  override def move(src: File, dest: File) {
  	val command = String.format("svn move %s %s", src.getCanonicalFile(), dest.getCanonicalFile())
    Command.execute(command)
  }
  
  override def init() {
    val projectName = dir.getName()
    val url = repo.url + File.separator + projectName + "/trunk"
    val command = String.format("""svn import . %s -m "Initial import of project"""", url)
    Command.execute(command)
    clone(url);
    
  }
  
  def cloneAndCheckout(url: String, revision: String) {
    clone(url + "@" + revision) 
  }
  
  override def clone(directory: File) {
    val command = "svn info"
    val info = SvnInfo.parse(Command.execute(command)(directory))
    clone(info.url)
  }
  
  override def clone(url: String) {
    val command = String.format("svn co %s .", url)
    Command.execute(command)
  }
  
  override def checkout(revision: String) {
  	clone(info.url + "@" + revision) 
  }
  
  override def add(file: File) {
    checkFilePath(file)
    val command = "svn add " + file.getCanonicalPath()
    Command.execute(command)
  }
  
  override def remove(file: File) {
    checkFilePath(file)
    val command = String.format("svn rm %s --force", file.getCanonicalPath()) 
   	Command.execute(command)    	
  }
  
  override def apply(patchFile: File) {
    //val command = "patch -p0 --binary -i " + patchFile.getCanonicalPath()
    //Command.execute(command)
    ApplyPatch.applyPatch(patchFile, dir)
  }
  
  def status: String = {
    val command = "svn st"
    val status = Command.execute(command)
    logger.info(status)
    status
  }
  
  override def hasChanges(): Boolean = {
  	val command = "svn status "
  	val output = Command.execute(command)
  	!output.trim.isEmpty()
  }
  
  def untrackedFiles(): Seq[String] = {
  	val output = status
  	val res =  output.split("\n").filter(_.startsWith("?")).map(_.replaceFirst("""\?\s++""", ""))
  	if (!res.isEmpty) {
  	  logger.info(output)
  	}
  	res
  }
  
  private def checkFilePath(file: File): Unit = {
    if (!file.getCanonicalFile().getParentFile().getCanonicalFile().toString().contains(dir.getCanonicalFile().toString())) {
      throw new RuntimeException(String.format("the file %s is not in the directory %s", file, dir))
    }
  }
}