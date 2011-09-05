package org.builder.util
import java.io.File

object FileUtils {

	def deleteFile(dfile : File) {
		if(dfile.isDirectory){
			dfile.listFiles.foreach{f => deleteFile(f)}
		}
		dfile.delete
	}
}