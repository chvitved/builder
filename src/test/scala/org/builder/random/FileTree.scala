package org.builder.random

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

abstract sealed class FileTree(name: String) {
	def size: Int = this match {
    	case FileLeaf(_, _) => 1
    	case Directory(name, children) => (children :\ 0) (_.size + _)
  	}
}

case class Directory(name: String, children: Seq[FileTree]) extends FileTree(name)
case class FileLeaf(name: String, fileType: FileType) extends FileTree(name)


abstract sealed case class FileType
case class Binary(size: Int) extends FileType
case class Text(size: Int) extends FileType

