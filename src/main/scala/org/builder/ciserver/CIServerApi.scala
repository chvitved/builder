package org.builder.ciserver
import java.net.HttpURLConnection
import java.net.URL
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.binary.Base64
import org.builder.util.UrlEncoder
import org.builder.util.BuildId
import java.util.regex.Pattern

class CIServerApi {
  
  def build(ciUrl: String, jobName:String, patchUrl: String, repoUrl: String, vcType: String): String = {
    
    val buildId = getNextBuildId(ciUrl, jobName)
    
	val encodedPatchUrl = UrlEncoder.encode(patchUrl)
	val encodedRepoUrl = UrlEncoder.encode(repoUrl)
	val revision = BuildId.getRevisionFromId(BuildId.fromPatchUrl(patchUrl))
	
	val ciBuildUrl = ciUrl + "/job/" + jobName + "/buildWithParameters"
	
	val url = new URL(String.format("%s?repourl=%s&vc=%s&patchurl=%s&rev=%s&token=builder", ciBuildUrl, encodedRepoUrl, vcType, encodedPatchUrl, revision))
    httpGet(url)
    ciUrl + "job/" + jobName + "/" + buildId + "/"
  }
  
  private def getNextBuildId(ciUrl: String, jobname: String) = {
    val url = new URL(String.format("%s/job/%s/api/json", ciUrl, jobname))
    val res = httpGet(url)
    val pattern = Pattern.compile(""""nextBuildNumber"\s*:\s*(\d+)""")
    val matcher = pattern.matcher(res)
    matcher.find
    val buildId = matcher.group(1)
    buildId
  }
    
  private def httpGet(url: URL): String = {
    val connection =  url.openConnection().asInstanceOf[HttpURLConnection]
    val authStringEnc = new String(Base64.encodeBase64("chr:4446aa5477f737cbc899ecd0ab1133ef".getBytes()))
    connection.setRequestProperty("Authorization", "Basic "+ authStringEnc);
    val statusCode = connection.getResponseCode() 
	if (statusCode!= 200) {
      val response = IOUtils.toString(connection.getErrorStream())
      throw new RuntimeException(String.format("Error when getting url %s\nGot response %s %S\nbody:\n%s", url.toString(), ""+statusCode, connection.getResponseMessage(), response))
	}
    IOUtils.toString(connection.getInputStream())
  }
  
}