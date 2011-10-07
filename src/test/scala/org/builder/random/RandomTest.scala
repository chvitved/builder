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
	import org.builder.buildserver.BuildServerStub

object RandomTest extends Properties("files") {
	
	val testDir = "randomtests"
	val origin = new File(testDir + File.separator + "origin")
	val repo1 = new File(testDir + File.separator + "repo1")
	
	val serverUrl = "http://localhost:7000"
	val buildserverDir = new File(testDir + File.separator + "buildserver")
	val buildServerStub = new BuildServerStub(buildserverDir, origin.getCanonicalPath(), serverUrl)
	Server.start(buildServerStub)
	
	
	implicit def arbFileTree: Arbitrary[FileTree] = Arbitrary(Generators.genFileTree)
	implicit def fileswithChanges: Arbitrary[(FileTree, Seq[Change])] = Arbitrary(Generators.genFilesWithChanges(repo1))

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
			if (!changes.isEmpty) {
				repo1Vc.commit("committing changes")
				org.apache.commons.io.FileUtils.forceMkdir(buildserverDir)
				val client = new Client(repo1, new ServerApi(serverUrl))
				client.build("test")
			}
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
				}
				case Edit(file, fileType, changes) => {
					editFile(file, fileType, changes)
					vc.add(file)
				}
				case Move(src, dest) => {
					vc.move(src, dest)
				}
			}
		}
	}
	
	private def editFile(file: File, fileType: FileType, changes: Seq[AFileChange]) {
		val bytes = org.apache.commons.io.FileUtils.readFileToByteArray(file)
		for(AFileChange(position, length) <- changes){
			val newBytes: Gen[Array[Byte]] = 
				fileType match {
					case Binary(_) =>  Generators.genBytes(length)
					case Text(_) =>  Generators.genTextBytes(length)
				}
			
			for{
				index <- 0 until length
				nb <- newBytes.sample
			} {
				val p = position + index
				if (p < bytes.size)
					bytes(p) = nb(index)
			}
		}
		val fos = new FileOutputStream(file)
		fos.write(bytes)
		fos.close
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
	  		writeBytes(name, parentPath, Generators.genBytes(size), vc)
	  	}
	  	case Text(size) => {
	  		writeBytes(name, parentPath, Generators.genTextBytes(size), vc) 
	  	}
	  }
	}
	
	private def writeBytes(name: String, parentPath: String, byteGenerator: Gen[Array[Byte]], vc: VersionControl) {
		val f = new File(parentPath, name)
		f.createNewFile();
		val fw = new BufferedOutputStream(new FileOutputStream(f))
		for {
	  		bytes <- byteGenerator.sample
		}  fw.write(bytes)
	  	fw.close
	  	vc.add(f)
	}
}