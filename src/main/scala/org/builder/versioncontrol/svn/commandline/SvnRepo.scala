package org.builder.versioncontrol.svn.commandline
import org.builder.command.Command
import java.io.File

object SvnRepo {
  def create(implicit dir: File) = {
    Command.execute("svnadmin create .")
    SvnRepo("file://" + dir.getCanonicalPath())
  }
}

case class SvnRepo(url: String)