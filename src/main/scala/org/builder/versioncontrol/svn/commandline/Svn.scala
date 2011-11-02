package org.builder.versioncontrol.svn.commandline

import java.io.File
import org.builder.versioncontrol.VersionControl
import org.builder.command.Command
import org.builder.versioncontrol.Patch

class Svn(directory: File, repo: SvnRepo) extends VersionControl{

  implicit val dir = directory
  
  override def commit(message: String) {
    val command = String.format("""svn commit -m "%s"""", message)
    Command.execute(command)
  }
  
  private def info(): SvnInfo = {
    val command = String.format("svn info")
    SvnInfo.parse(Command.execute(command))
  }
  
  override def getLastCommitIdFromOrigin(): String = {
    info.revision
  }
  
  override def createPatch(f: File): Patch = {
    val svnInfo = info
    val id = svnInfo.revision
    val command = String.format("""svn diff --force --diff-cmd diff -x "-au --binary"""")
    Command.execute(command, f.getCanonicalFile())
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
    val command = "patch -p0 --binary -i " + patchFile.getCanonicalPath()
    Command.execute(command)
  }
  
  override def hasChanges(): Boolean = {
  	val command = "svn status "
  	val output = Command.execute(command)
  	!output.trim.isEmpty()
  }
  
  def untrackedFiles(): Seq[String] = {
  	val command = "svn status"
  	val output = Command.execute(command)
  	output.split("\n").filter(_.startsWith("?")).map(_.replaceFirst("""\?\s++""", ""))
  }
  
  private def checkFilePath(file: File): Unit = {
    if (!file.getCanonicalFile().getParentFile().getCanonicalFile().toString().contains(dir.getCanonicalFile().toString())) {
      throw new RuntimeException(String.format("the file %s is not in the directory %s", file, dir))
    }
  }
}