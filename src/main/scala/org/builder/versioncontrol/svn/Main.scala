package org.builder.versioncontrol.svn

import org.tmatesoft.svn.core.wc.SVNClientManager
import java.io.File
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.SVNDepth

object Main {

  def main(args: Array[String]): Unit = {
    
    val cm = SVNClientManager.newInstance()
    val diffClient =  cm.getDiffClient()
    diffClient.doDiff(new File("/tmp/pl2"), SVNRevision.UNDEFINED, SVNRevision.WORKING, SVNRevision.BASE,
    		SVNDepth.INFINITY, true, System.out, null);
  }
  
}