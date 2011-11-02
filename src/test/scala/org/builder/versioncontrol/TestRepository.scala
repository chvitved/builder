package org.builder.versioncontrol

import org.builder.versioncontrol.git.commandline.Git
import org.builder.versioncontrol.svn.commandline.Svn
import org.junit._
import Assert._
import java.io.File
import java.io.FileWriter
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import org.builder.util.FileUtils
import org.builder.versioncontrol.svn.commandline.SvnRepo

@Ignore
class TestRepository(val vc: VersionControl) {
	
	def dir = vc.dir
  
	def init() {
		vc.init()
	}
	
	def hasChanges() = vc.hasChanges()
	
	def untrackedFiles() = vc.untrackedFiles()
	
	def clone(repo: TestRepository) {
	  vc.clone(repo.dir)
	}
	
	def commit(msg: String) {
	  vc.commit(msg)
	}
	
	def createPatch(file: File): Patch = {
	  vc.createPatch(file)
	}
	
	def applyPatch(f: File) {
		vc.apply(f)
	}
	
	def getLastCommitAtOrigin(): String = {
	  vc.getLastCommitIdFromOrigin()
	}
	
	def getFileAsString(fileName: String) = {
		IOUtils.toString(new FileInputStream(new File(vc.dir, fileName)))
	}
	
	def getLastCommitIdFromOrigin() = {
	  vc.getLastCommitIdFromOrigin()
	}

	def createNewFile(name: String, content: String, addToVC: Boolean) {
		val newFile = new File(vc.dir, name)
		writeToFile(newFile, content)
		if (addToVC) {
			vc.add(newFile)			
		}
	}
	
	def editFile(name: String, content: String){
	  val f = new File(vc.dir, name)
	  if (!f.exists()) {
	    throw new RuntimeException(String.format("file %s does not exists", f))
	  }
	  writeToFile(f, content)
	  //vc.add(f)
	}

	private def writeToFile(file: File, content: String): Unit = {
	  val fw = new FileWriter(file)
	  fw.write(content)
	  fw.close()
	}
	
  }