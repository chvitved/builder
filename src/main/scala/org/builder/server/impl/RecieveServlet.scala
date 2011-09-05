package org.builder.server.impl
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.commons.io.IOUtils
import org.builder.buildserver.BuildserverApi
import org.builder.util.BuildId
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.zip.GZIPInputStream

class RecieveServlet(buildserver: BuildserverApi) extends HttpServlet{
  
  val storageDir = "storage"
  
  override def init() {
    val dir = new File(storageDir)
    if (!dir.exists()) {
      dir.mkdir()
    }
  }
    
  /**
   * receives new builds
   */
  override def doPost(req: HttpServletRequest, res: HttpServletResponse) {
	 val projectName = req.getParameter("project")
	 val revision = req.getParameter("revision")
	 val buildId = BuildId.createBuildId(projectName, revision)
	 val newFileStream =  new BufferedOutputStream(new FileOutputStream(getFileName(buildId)))
	 IOUtils.copy(req.getInputStream(), newFileStream)
	 newFileStream.close()
	 buildserver.build(buildId, projectName)
	 
	 val buildUrl =  String.format("http://%s/build/?buildid=%s", req.getHeader("Host"), buildId)
	 res.setHeader("Location",buildUrl)
	 res.setStatus(201) //created
  }
  
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) {
  	val buildId = req.getParameter("buildid")
  	val fileStream = new BufferedInputStream(new FileInputStream(getFileName(buildId)))
  	res.setContentType("application/x-gzip")
    val outStream =  res.getOutputStream()
  	IOUtils.copy(fileStream,outStream)
  	fileStream.close()
  	outStream.close()
  }
  
  private def getFileName(buildId: String): String = {
    storageDir + "/" + buildId + ".patch.zip"
  }
}