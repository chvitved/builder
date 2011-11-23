package org.builder.random
import org.builder.versioncontrol.VersionControl
import java.io.File
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import org.scalacheck.Gen
import org.builder.util.FileUtils

object ManipulateFiles {
	def createChanges(changes: Seq[Change], vc: VersionControl) {
	    val parentPath = vc.dir.getPath()
		for (c <- changes) {
			println("change " + c)
			c match {
				case AddFile(file, fileType) => {
				  FileUtils.createDir(file.getCanonicalFile().getParentFile())
				  writeFileData(file.getName(), fileType, concatPath(parentPath, file.getParent()), vc)
				}
				case AddDir(file) => {
				  val dir = prependPath(parentPath, file)
				  org.apache.commons.io.FileUtils.forceMkdir(dir)
				  vc.add(dir)
				}
				case Remove(file) => {
				  val f = prependPath(parentPath, file)
				  vc.remove(f)
				  f.delete();
				}
				case Edit(file, fileType, changes) => {
				    val f = prependPath(parentPath, file)
					editFile(f, fileType, changes)
					vc.add(f)
				}
				case Move(src, dest) => {
				  val s = prependPath(parentPath, src)
				  val d = prependPath(parentPath, dest)
				  vc.move(s, d)
				  
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
		  val dir = createDir(parentPath, vc, name)
		  for(c <- children) doCreateFiles(c, dir.getPath(), vc)
		}
	  }
	}
	
	private def createDir(parentPath: String, vc: org.builder.versioncontrol.VersionControl, name: String): java.io.File = {
	  val parent = new File(parentPath)
	  FileUtils.createDir(parent)
	  val dir = new File(parent, name)
	  FileUtils.createDir(dir)
	  vc.add(dir)
	  dir
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