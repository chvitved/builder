	package org.builder.random

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

import org.builder.util.FileUtils
import org.builder.versioncontrol.git.commandline.Git
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Properties

object RandomTest extends Properties("files") {
	
	val testDir = "randomtests"
	val origin = new File(testDir + File.separator + "origin")
	val repo1 = new File(testDir + File.separator + "repo1")
	
	val vc = new Git(origin)
	
	implicit def arbFileTree: Arbitrary[FileTree] = Arbitrary(Generators.genFileTree)

	var counter = 0	
	property("tree") =  Prop.forAll((files: FileTree) => {
		org.apache.commons.io.FileUtils.forceMkdir(origin)
		
		vc.init()
		createFiles(files)
		vc.commit("committet all files")
		
		org.apache.commons.io.FileUtils.forceMkdir(repo1)
		
		val repo1Vc = new Git(repo1)
		repo1Vc.clone(origin.getCanonicalPath())
		
		
		counter += 1
		println(counter)

		
		
		removeFiles();
		true
	})
	
	private def removeFiles() {	
		FileUtils.deleteFile(new File(testDir));
	}
	
	private def createFiles(files: FileTree) {
		val originDir = new Directory("origin", List(files))
		val randomTestsDir = new Directory(testDir, List(originDir))
		doCreateFiles(randomTestsDir, null)
	}
	
	private def doCreateFiles(files: FileTree, parentPath: String) {
		files match {
			case FileLeaf(name, fileType) => {
			    writeFileData(name, fileType, parentPath)
			}
			case Directory(name, children) =>
				val dir = new File(parentPath, name)
				dir.mkdir()
				for(c <- children) doCreateFiles(c, dir.getPath())
		}
	}
	
	private def writeFileData(name: String, fileType: FileType, parentPath: String) {
	  fileType match {
	  	case Binary(size) => {
	  		writeBytes(name, parentPath, size, Generators.genByte)
	  	}
	  	case Text(size) => {
	  		writeBytes(name, parentPath, size, Generators.genTextByte)
	  	}
	  }
	}
	
	private def writeBytes(name: String, parentPath: String, size: Int, byteGenerator: Gen[Byte]) {
		val f = new File(parentPath, name)
		f.createNewFile();
		val fw = new BufferedOutputStream(new FileOutputStream(f))
		for (
	  		i <- 0 until size;
	  		byte <- byteGenerator.sample
	  	)  fw.write(byte)
	  	fw.close
	  	vc.add(f)
	}
}