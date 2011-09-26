package org.builder.random
import java.io.File

object FileTree {
	def print(files: FileTree, indention: Int) {
		files match {
			case FileLeaf(name, fileType) => println(String.format("%s%sfile %s", indent(indention), fileType, name))
			case Directory(name, children) =>  {
				println(String.format("%sdir %s", indent(indention), name))
				for (c <- children) print(c, indention + 1)
			}
		}
	}
	
	private def indent(spaces: Int): String = {
		var builder = new StringBuilder()
		for(i <- 0 to spaces) {
			builder.append(" ")
		}
		builder.toString
	}
	
}

abstract sealed class FileTree(name: String){
	def size: Int = this match {
    	case FileLeaf(_, _) => 1
    	case Directory(name, children) => (children :\ 0) (_.size + _)
  	}
	
	def getFiles() : Seq[(File, FileType)] = {
		def doGet(tree: FileTree, parentPath: File, result: Seq[(File, FileType)]): Seq[(File, FileType)] = {
			tree match {
				case FileLeaf(name, fileType) => (new File(parentPath, name), fileType) +: result
				case Directory(name, children) =>  {
					val newPath = new File(parentPath, name) 
					val newResult = (newPath, null) +: result
					(children :\ newResult) ((tree, seq) => doGet(tree, newPath, seq))
				}
			}
		}
		doGet(this, new File(""), Seq())
	}
}

case class Directory(name: String, children: Seq[FileTree]) extends FileTree(name)
case class FileLeaf(name: String, fileType: FileType) extends FileTree(name)


abstract sealed case class FileType(size: Int)
case class Binary(s: Int) extends FileType(s)
case class Text(s: Int) extends FileType(s)

