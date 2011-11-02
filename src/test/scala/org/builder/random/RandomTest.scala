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
import java.util.Arrays

object RandomTest extends Properties("files") {

	val testDirName = "randomtests"
	val originDirName = "origin"
	val repo1DirName = "repo1"
	val repo1 = new File(testDirName + File.separator + repo1DirName)
	val repo1Vc = new Git(repo1)  
	  
	val origin = new File(testDirName + File.separator + originDirName)
	val rootVc = new Git(origin)

	
	val serverUrl = "http://localhost:7000"
	val buildserverDir = new File(testDirName + File.separator + "buildserver")
	val buildserverVc = new Git(buildserverDir);
	
	val buildServerStub = new BuildServerStub(buildserverVc, origin.getCanonicalPath(), serverUrl)
	Server.start(buildServerStub)


	implicit def fileswithChanges: Arbitrary[(FileTreeRoot, Seq[Change])] = Arbitrary(Generators.genFilesWithChanges)

	var counter = 0;
	
	property("tree") =  Prop.forAll((tuple: (FileTreeRoot, Seq[Change])) => {
	  val files = tuple._1
	  val changes = tuple._2.reverse
	  debugOutput(files, changes)			
	  try {
	    FileUtils.deleteFile(origin)
	    FileUtils.deleteFile(repo1)
	    FileUtils.createDir(origin)
	    FileUtils.createDir(repo1)
		rootVc.init()
		createFiles(files, rootVc)
		rootVc.commit("committet all files")

		repo1Vc.clone(origin)
		createChanges(changes, repo1Vc)
		if (!changes.isEmpty) {
			org.apache.commons.io.FileUtils.forceMkdir(buildserverDir)
			val client = new Client(repo1Vc, new ServerApi(serverUrl))
			client.build("test")
			FileUtils.compareFiles(buildserverDir, repo1)
		} else {
			true				
		}
	  } finally {
		removeFiles();
		println()
	  }
	})
	
	private def debugOutput(files: FileTreeRoot, changes: Seq[Change]) {
	  counter += 1
	  println("test number " + counter)
	  println("files:")
	  FileTree.print(files)
	  println("change")
	  for(c <- changes) println(c)
	  println("-------------")
	}
	
	private def concatPath(parentPath: String, path: String) : String = {
	  val pp = if (parentPath == null) "" else parentPath + File.separatorChar
	  val p = if (path == null) "" else path
	  pp + p
	}
	
	private def prependPath(parentPath: String, file: File) : File = {
	  val p = if (file.getParent() != null) file.getParent() else "" 
	  new File(parentPath + File.separatorChar + p, file.getName())
	}

	private def createChanges(changes: Seq[Change], vc: VersionControl) {
	    val parentPath = vc.dir.getPath()
		for (c <- changes) {
			println("change " + c)
			c match {
				case AddFile(file, fileType) => {
					writeFileData(file.getName(), fileType, concatPath(parentPath, file.getParent()), vc)
				}
				case AddDir(file) => {
					org.apache.commons.io.FileUtils.forceMkdir(prependPath(parentPath, file))
				}
				case Remove(file) => {
					if (!(file.isDirectory() && file.list().isEmpty)) {
						vc.remove(prependPath(parentPath, file))						
					}
				}
				case Edit(file, fileType, changes) => {
				    val f = prependPath(parentPath, file)
					editFile(f, fileType, changes)
					vc.add(f)
				}
				case Move(src, dest) => {
				  val s = prependPath(parentPath, src)
				  if (!(s.isDirectory() && s.list().isEmpty)) {
				    val d = prependPath(parentPath, dest)
					vc.move(s, d)
				  }
				}
			}
		}
	}

	private def editFile(file: File, fileType: FileType, changes: Seq[AFileChange]) {
		val bytes = org.apache.commons.io.FileUtils.readFileToByteArray(file)
		for(AFileChange(position, length) <- changes){
			val newBytes = 
			fileType match {
				case Binary(_) =>  Generators.genBytes(length).sample.get
				case Text(_) =>  Generators.genTextBytes(length).sample.get
			}
			for(index <- 0 until length) {
   			  val p = position + index
   			  if (p < bytes.size && newBytes.size > index)
				bytes(p) = newBytes(index)
			}
		}
		val fos = new FileOutputStream(file)
		fos.write(bytes)
		fos.close
	}

	private def removeFiles() {	
		FileUtils.deleteFile(new File(testDirName));
	}
	
	def createFiles(files: FileTreeRoot, vc: VersionControl) {
	  val parentPath = vc.dir.getCanonicalPath()
	  for(c <- files.children)
		  doCreateFiles(c, parentPath, vc)
	}

	private def doCreateFiles(files: FileTree, parentPath: String, vc: VersionControl) {
	  files match {
	  	case FileLeaf(name, fileType) => {
		  writeFileData(name, fileType, parentPath, vc)
		}
		case Directory(name, children) => {
		  val dir = new File(parentPath, name)
		  dir.mkdir()
		  for(c <- children) doCreateFiles(c, dir.getPath(), vc)
		}
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