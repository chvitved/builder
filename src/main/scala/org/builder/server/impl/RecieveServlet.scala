package org.builder.server.impl
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.commons.io.IOUtils
import org.builder.util.BuildId
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.zip.GZIPInputStream
import org.builder.ciserver.CIServerApi
import java.net.URLDecoder
import org.builder.util.UrlEncoder
import org.builder.versioncontrol.VCType

class RecieveServlet(ciServer: CIServerApi) extends HttpServlet{
  
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
	 val ciUrl = UrlEncoder.decode(req.getParameter("ciurl"))
	 val jobName = UrlEncoder.decode(req.getParameter("job"))
	 val revision = req.getParameter("revision")	 
	 val repoUrl = URLDecoder.decode(req.getParameter("repo"), "UTF-8")
	 val vc = req.getParameter("vc")
	 val buildId = BuildId.createBuildId(jobName, revision, VCType.fromString(vc));
	 
	 val newFileStream =  new BufferedOutputStream(new FileOutputStream(getFileName(buildId)))
	 IOUtils.copy(req.getInputStream(), newFileStream)
	 newFileStream.close()
	 val patchUrl = String.format("%s?buildid=%s", req.getRequestURL(), buildId)
	 val buildUrl = ciServer.build(ciUrl, jobName, patchUrl, repoUrl, vc)
	 
	 //val buildUrl =  String.format("http://%s/build/?buildid=%s", req.getHeader("Host"), buildId)
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