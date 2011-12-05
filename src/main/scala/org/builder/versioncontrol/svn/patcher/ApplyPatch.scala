package org.builder.versioncontrol.svn.patcher
import java.io.File
import scala.collection._
import java.util.zip.ZipOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.util.zip.ZipFile
import java.io.BufferedOutputStream
import java.io.BufferedInputStream
import org.apache.commons.io.FileUtils
import java.io.InputStream
import org.apache.log4j.Logger

object ApplyPatch {
  
  val logger = Logger.getLogger(classOf[ApplyPatch])
  
  
  def applyPatch(patch: File, repoDir: File) {
    val zipFile = new ZipFile(patch)
    val fileEnumeration = zipFile.entries()
    while(fileEnumeration.hasMoreElements()) { {
        val ze = fileEnumeration.nextElement()
        if (ze.getName() == CreatePatch.deletesFilename) {
          handleDeleteFile(zipFile.getInputStream(ze), repoDir)
        } else {
          writeFile(zipFile.getInputStream(ze), ze, repoDir)
        }
      }
    }
  }
  
  private def writeFile(zipEntryInputStream: InputStream, ze: java.util.zip.ZipEntry, repoDir: File): Unit = {
    val outputFile = new File(repoDir, ze.getName())
    FileUtils.forceMkdir(outputFile.getParentFile())
    val outputFileStream = new BufferedOutputStream(new FileOutputStream(outputFile));
    try {
    	IOUtils.copy(zipEntryInputStream, outputFileStream)
    } finally {
    	zipEntryInputStream.close()
    	outputFileStream.close()
    }
  }
  
  private def handleDeleteFile(zipEntryInputStream: InputStream, repoDir: File) {
    val lines = IOUtils.lineIterator(zipEntryInputStream, "UTF-8")
    while(lines.hasNext()) {
      val file = new File(repoDir, lines.nextLine())
      val deleted = file.delete()
      if (!deleted) {
        logger.error("could not delete file " + file)
      }
    }
  }
  
}
class ApplyPatch{}