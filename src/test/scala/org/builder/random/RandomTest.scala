package org.builder.random

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import org.builder.util.FileUtils
import org.builder.versioncontrol.git.commandline.Git
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Properties
import org.scalacheck.Prop
import org.builder.versioncontrol.VersionControl
import org.builder.client.Client
import org.builder.server.api.ServerApi
import org.builder.server.impl.Server
import org.builder.ciserver.CIServerStub
import java.util.Arrays

object RandomTest extends Properties("files") {
  
  val baseDir = new File("randomtests")
  FileUtils.deleteFile(baseDir)
  val scenarioFactory = new ScenarioFactory(baseDir)
  
	implicit def fileswithChanges: Arbitrary[(FileTreeRoot, Seq[Change])] = Arbitrary(Generators.genFilesWithChanges)

	var counter = 0;
	
	property("tree") =  Prop.forAll((tuple: (FileTreeRoot, Seq[Change])) => {
	  val files = tuple._1
	  val changes = tuple._2.reverse
	  debugOutput(files, changes)			
	  forAll((origin: VersionControl, repo1: VersionControl, client: Client, buildserverDir: File) => {
          println("running test with " + origin.getClass)
		  origin.init()
          ManipulateFiles.createFiles(files, origin)
          origin.commit("committet all files")

          repo1.clone(origin.dir)
          ManipulateFiles.createChanges(changes, repo1)
          client.build("http://test.com")
          FileUtils.compareFiles(buildserverDir, repo1.dir)
	  })
	})
	
	private def forAll(method: (VersionControl, VersionControl, Client, File) => Boolean) : Boolean = {
	  val vcConfigs: Seq[Scenario] = scenarioFactory.scenarios();
	  try {
	    vcConfigs.forall(vcConfig => method(vcConfig.origin, vcConfig.repo1, vcConfig.client, vcConfig.buildserverDir))
	  } finally {
		println()
	  }
	}
	
	private def debugOutput(files: FileTreeRoot, changes: Seq[Change]) {
	  counter += 1
	  println("test number " + counter)
	  println("files:")
	  FileTree.print(files)
	  println("change")
	  for(c <- changes) println(c)
	  println("-------------")
	}

}