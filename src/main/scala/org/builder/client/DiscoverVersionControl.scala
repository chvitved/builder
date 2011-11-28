package org.builder.client

import java.io.{File, FilenameFilter}
import org.builder.versioncontrol.VersionControl
import org.builder.versioncontrol.svn.commandline.Svn
import org.builder.versioncontrol.git.commandline.Git

object DiscoverVersionControl {
  
  def discover(dir: File): VersionControl = {
    val children = dir.list()
    if (children.contains(".svn")) {
       new Svn(dir, null)
    } else if (children.contains(".git")) {
      new Git(dir)
    } else {
      throw new RuntimeException(String.format("is %s under version control?", dir.getCanonicalPath));
    }
  }

}