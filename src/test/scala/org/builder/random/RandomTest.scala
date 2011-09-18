	package org.builder.random

import java.io.File
import java.io.FileOutputStream
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Properties
import java.io.BufferedOutputStream
import org.scalacheck.Gen

object RandomTest extends Properties("files") {
	
	val testDir = "randomtests"
	
	implicit def arbFileTree: Arbitrary[FileTree] = Arbitrary(Generators.genFileTree)

	property("tree") =  Prop.forAll((files: FileTree) => {
		createFiles(files)
		removeFiles();
		true
	})
	
	private def removeFiles() {	
		new File(testDir).delete()
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
	}
}