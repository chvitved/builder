package org.builder.random

import org.scalacheck._

import java.io.File

object RandomTest extends Properties("String") {
	
	val testDir = "randomtests"
	
	implicit def arbFileTree: Arbitrary[FileTree] = Arbitrary(Generators.genFileTree)

	property("tree") =  Prop.forAll((files: FileTree) => {
		
	})
	
	
	
	def removeFiles() {
		new File(testDir).delete()
	}
	
	def createFiles(files: FileTree) {
		val originDir = new Directory("origin", List(files))
		val randomTestsDir = new Directory(testDir, List(originDir))
		doCreateFiles(randomTestsDir)
	}
	
	def doCreateFiles(files: FileTree) {
		files match {
			case FileLeaf(name) => {
				val f = new File(name)
				f.createNewFile();
			}
			case Directory(name, children) =>
				val dir = new File(name)
				dir.mkdir()
				for(c <- children) doCreateFiles(c)
		}
	}
		
}