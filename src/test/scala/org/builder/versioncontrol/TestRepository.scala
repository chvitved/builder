package org.builder.versioncontrol

import org.builder.versioncontrol.git.commandline.Git
import org.junit._
import Assert._
import java.io.File
import java.io.FileWriter
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import org.builder.util.FileUtils

@Ignore
class TestRepository(baseDir: File, name: String) {
	
	createDir(baseDir)
	val dir = new File(baseDir, name)
	if (dir.exists()) {
		FileUtils.deleteFile(dir);
	}
	createDir(dir)
	val vc = new Git(dir)
	
	
	
	def init(): TestRepository = {
		vc.init()
		this
	}
	
	def hasChanges() = vc.hasChanges()
	
	def clone(dir: File): TestRepository = {
	  vc.clone(dir.getCanonicalPath())
	  this
	}
	
	def commit(msg: String) {
	  vc.commit(msg)
	}
	
	def createPatch(): Patch = {
	  vc.createPatch()
	}
	
	def applyPatch(f: File) {
		vc.apply(f)
	}
	
	def getLastCommitAtOriginMaster(): String = {
	  vc.getLastCommitIdAtOriginMaster()
	}
	
	def getLatestRevision(branch: String): String = {
	  vc.getLatestRevision(branch)
	}
	
	private def createDir(dir: File) {
	  if (!dir.exists()) {
		dir.mkdir()
	  }
	}
	
	def getFileAsString(fileName: String) = {
		IOUtils.toString(new FileInputStream(new File(dir, fileName)))
	}

	def createNewFile(name: String, content: String, addToVC: Boolean): File = {
		val newFile = new File(dir, name)
		writeToFile(newFile, content)
		if (addToVC) {
			vc.add(newFile)			
		}
		newFile
	}
	
	def editFile(name: String, content: String): File = {
	  val f = new File(dir, name)
	  if (!f.exists()) {
	    throw new RuntimeException(String.format("file %s does not exists", f))
	  }
	  writeToFile(f, content)
	  vc.add(f)
	  f
	}

	private def writeToFile(file: File, content: String): Unit = {
	  val fw = new FileWriter(file)
	  fw.write(content)
	  fw.close()
	}
	
	def removeRepository {
		FileUtils.deleteFile(dir)
	}
  }