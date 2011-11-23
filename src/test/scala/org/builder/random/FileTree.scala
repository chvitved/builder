package org.builder.random
import java.io.File

object FileTree {
    def print(fileTreeRoot: FileTreeRoot) {
      for(c <- fileTreeRoot.children) print(c, 0)
    }
  
	def print(files: FileTree, indention: Int) {
		files match {
			case FileLeaf(name, fileType) => println(String.format("%s%s %s", indent(indention), name, fileType))
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

trait FileTree{
	val name: String
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
		doGet(this, new File(".").getCanonicalFile(), Seq())
	}
}

case class FileTreeRoot(children: Seq[FileTree]) {
  def size: Int = (children :\0) (_.size + _)
  def getFiles() : Seq[(File, FileType)] = (children :\ Seq[(File, FileType)]()) ( (fileTree, seq) => fileTree.getFiles ++ seq)
}
case class Directory(name: String, children: Seq[FileTree]) extends FileTree
case class FileLeaf(name: String, fileType: FileType) extends FileTree


abstract sealed case class FileType(size: Int)
case class Binary(s: Int) extends FileType(s)
case class Text(s: Int) extends FileType(s)

