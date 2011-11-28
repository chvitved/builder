package org.builder.ciserver
import java.net.HttpURLConnection
import java.net.URL
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.binary.Base64;
import org.builder.util.Base64Encoder

class CIServerApi {
  
  def build(ciUrl: String, jobName:String, repoUrl: String, patchUrl: String): String = {
    
	val authStringEnc = Base64Encoder.encode("chr:password")
	val encodedPatchUrl = Base64Encoder.encode(patchUrl)
	val encodedRepoUrl = Base64Encoder.encode(repoUrl)
	
	val ciBuildUrl = ciUrl + "/job/" + jobName + "/buildWithParameters"
	
	val url = new URL(String.format("%s?repourl=%s&patchurl=%s&token=builder", ciUrl, repoUrl, patchUrl))
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