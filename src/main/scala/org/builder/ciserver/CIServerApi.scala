package org.builder.ciserver
import java.net.HttpURLConnection
import java.net.URL
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.binary.Base64
import org.builder.util.UrlEncoder
import org.builder.util.BuildId

class CIServerApi {
  
  def build(ciUrl: String, jobName:String, patchUrl: String, repoUrl: String, vcType: String): String = {
    
	val authStringEnc = new String(Base64.encodeBase64("chr:password".getBytes()))
	val encodedPatchUrl = UrlEncoder.encode(patchUrl)
	val encodedRepoUrl = UrlEncoder.encode(repoUrl)
	val revision = BuildId.getRevisionFromId(BuildId.fromPatchUrl(patchUrl))
	
	val ciBuildUrl = ciUrl + "/job/" + jobName + "/buildWithParameters"
	
	val url = new URL(String.format("%s?repourl=%s&vc=%s&patchurl=%s&rev=%s&token=builder", ciBuildUrl, encodedRepoUrl, vcType, encodedPatchUrl, revision))
	println(url)
    val connection =  url.openConnection().asInstanceOf[HttpURLConnection]
	connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
    val statusCode = connection.getResponseCode() 
    if (statusCode!= 200) {
      val response = IOUtils.toString(connection.getErrorStream())
      throw new RuntimeException(String.format("Error when notifying build server. \n using url %s\nGot response %s %S\nbody:\n%s", url.toString(), ""+statusCode, connection.getResponseMessage(), response))
    }
    url.getPath()
  }
}