package org.builder.versioncontrol.svn.patcher
import java.util.zip.ZipOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.io.BufferedOutputStream
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import java.io.BufferedInputStream
import java.io.FileInputStream

object CreatePatch {
  val deletesFilename = "__builder_deleteFile__" 
  
  val statusForRemoval = Set[Char]('D', 'I')
  val statusForAddition = Set[Char]('A', 'M', 'R')

  def createPatchFromStatus(svnStatus: String, repoDir: File, outputFile: File) {
    val statuses = parseStatus(svnStatus)
    val add = statuses.filter(s => statusForAddition.contains(s.status)).map(_.file)
    val remove = statuses.filter(s => statusForRemoval.contains(s.status)).map(_.file)
    createZipFile(add, remove, repoDir, outputFile)
  }
  
  private def parseStatus(svnStatus: String): Seq[FileStatus] = {
    val lines = svnStatus.split("\n")
    for{line <- lines
      statusChar = line.charAt(0)
      fileStr = line.substring(8, line.length())
    }
      yield FileStatus(statusChar, new File(fileStr))
  }
  
  private def createZipFile(filestoAdd: Seq[File], filestoRemove: Seq[File], repoDir: File, outputFile: File) {
	val out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
    try {
	    //add files
	    for{
	    	f <- filestoAdd
	    	if (new File(repoDir, f.getPath()).isFile())
	    } {
	    	out.putNextEntry(new ZipEntry(f.getPath()));
	    	IOUtils.copy(new BufferedInputStream(new FileInputStream(new File(repoDir, f.getPath()))), out)
	    	out.closeEntry()
	    }
	    //create one file, listing all files to delete
	    out.putNextEntry(new ZipEntry(CreatePatch.deletesFilename));
	    for(f <- filestoRemove) {
    	val bytes = (f.getPath() + "\n").getBytes("UTF-8")
    	out.write(bytes)
	    }
	    out.close
    } catch {
    	case e: Exception => {
    	  out.close
    	  outputFile.delete();
    	}
    	throw e
    }
  }

  case class FileStatus(status: Char, file: File)
}