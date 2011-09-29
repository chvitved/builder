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

object RandomTest extends Properties("files") {
	
	val testDir = "randomtests"
	val origin = new File(testDir + File.separator + "origin")
	val repo1 = new File(testDir + File.separator + "repo1")
	
	implicit def arbFileTree: Arbitrary[FileTree] = Arbitrary(Generators.genFileTree)
	implicit def fileswithChanges: Arbitrary[(FileTree, Seq[Change])] = Arbitrary(Generators.genFilesWithChanges(repo1))

//	property("tree") =  Prop.forAll((files: FileTree) => {
//		org.apache.commons.io.FileUtils.forceMkdir(origin)
//		
//		vc.init()
//		createFiles(files)
//		vc.commit("committet all files")
//		
//		org.apache.commons.io.FileUtils.forceMkdir(repo1)
//		
//		val repo1Vc = new Git(repo1)
//		repo1Vc.clone(origin.getCanonicalPath())
//		
//		
//		counter += 1
//		println(counter)
//
//		
//		
//		removeFiles();
//		true
//	})
	
	var counter = 0;
	property("tree") =  Prop.forAll((tuple: (FileTree, Seq[Change])) => {
		try {
			val files = tuple._1
			val changes = tuple._2.reverse
			println("files:")
			FileTree.print(tuple._1, 0)
			println("change")
			for(c <- tuple._2) println(c)
			println("-------------")
			
			
			val rootVc = new Git(origin)
			
			org.apache.commons.io.FileUtils.forceMkdir(origin)
			rootVc.init()
			createFiles(files, rootVc)
			if (rootVc.hasChanges()) {
				rootVc.commit("committet all files")
			}
			
			
			org.apache.commons.io.FileUtils.forceMkdir(repo1)
			
			val repo1Vc = new Git(repo1)
			repo1Vc.clone(origin.getCanonicalPath())
			createChanges(changes, repo1Vc)
			
			
			counter += 1
			println("test number " + counter)
			println()
			true
		} finally {
			removeFiles();
		}
	})
	
	private def createChanges(changes: Seq[Change], vc: VersionControl) {
		for (c <- changes) {
			println("change " + c)
			c match {
				case AddFile(file, fileType) => {
					writeFileData(file.getName(), fileType, file.getParent(), vc)
				}
				case AddDir(file) => {
					org.apache.commons.io.FileUtils.forceMkdir(file)
				}
				case Remove(file) => {
					if (!(file.isDirectory() && file.list().isEmpty)) {
						vc.remove(file)						
					}
					FileUtils.deleteFile(file)
				}
				case Edit(file, changes) => {
					//TODO
				}
				case Move(src, dst) => {
					//TODO
				}
			}
		}
	}
	
	
	private def removeFiles() {	
		FileUtils.deleteFile(new File(testDir));
	}
	
	private def createFiles(files: FileTree, vc: VersionControl) {
		val originDir = new Directory("origin", List(files))
		val randomTestsDir = new Directory(testDir, List(originDir))
		doCreateFiles(randomTestsDir, null, vc)
	}
	
	private def doCreateFiles(files: FileTree, parentPath: String, vc: VersionControl) {
		files match {
			case FileLeaf(name, fileType) => {
			    writeFileData(name, fileType, parentPath, vc)
			}
			case Directory(name, children) =>
				val dir = new File(parentPath, name)
				dir.mkdir()
				for(c <- children) doCreateFiles(c, dir.getPath(), vc)
		}
	}
	
	private def writeFileData(name: String, fileType: FileType, parentPath: String, vc: VersionControl) {
	  fileType match {
	  	case Binary(size) => {
	  		writeBytes(name, parentPath, size, Generators.genByte, vc)
	  	}
	  	case Text(size) => {
	  		writeBytes(name, parentPath, size, Generators.genTextByte, vc)
	  	}
	  }
	}
	
	private def writeBytes(name: String, parentPath: String, size: Int, byteGenerator: Gen[Byte], vc: VersionControl) {
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