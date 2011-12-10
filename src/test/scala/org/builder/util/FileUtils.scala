package org.builder.util
import java.io.{File, FilenameFilter}
import scala.util.Sorting

import org.apache.commons.io.{FileUtils => IoFileUtils}

object FileUtils {

	def deleteFile(dfile : File) {
		if(dfile.isDirectory){
			dfile.listFiles.foreach{f => deleteFile(f)}
		}
		dfile.delete
	}
	
	def createDir(dir: File) {
	  IoFileUtils.forceMkdir(dir)
	}
	
	def compareFiles(f1: File, f2: File): Boolean = {
		if (f1.isDirectory() && f2.isDirectory()) {
			val filenameFiler = new FilenameFilter() {
				def accept(dir: File, name: String): Boolean = {
					!(
					  (dir.isDirectory() && name == ".git") || 
					  (dir.isDirectory() && name == ".svn") ||
					  (dir.isDirectory() && dir.list().isEmpty)
					)
				}
			}
			val files1 = f1.list(filenameFiler)
			val files2 = f2.list(filenameFiler)
			if (files1.length != files2.length) {
			  false
			} else {
				Sorting.quickSort(files1)
				Sorting.quickSort(files2)
				(files1 zip files2).forall(ft => compareFiles(new File(f1, ft._1), new File(f2, ft._2)))
			}
		} else if (f1.isFile() && f2.isFile()){
			val res = f1.getName() == f2.getName() && f1.length() == f2.length() //hope this is good enough, will not compute the md5 of the data or something like that
			if (!res)  debugError(f1, f2)
			res
		} else {
			debugError(f1, f2)
			false
		}
	}
	
	private def debugError(f1: File, f2: File) {
		println(String.format("files did not match. %s %s. with lengths %s and %s", f1.getCanonicalPath(), f2.getCanonicalPath(), "" + f1.length, "" + f2.length))
	}
}
