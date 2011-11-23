package org.builder.ciserver
import java.net.HttpURLConnection
import java.net.URL
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.binary.Base64;

class CIServerApi {
  
  def build(repoUrl: String, buildId: String, ciUrl: String, patchUrl: String) {
    
	val authStringEnc = encode("chr:chr")
	val encodedPatchUrl = encode(patchUrl)
	val encodedRepoUrl = encode(repoUrl)
	
	val url = new URL(String.format("%s/buildWithParameters?id=%s&patchurl=%s&token=builder", ciUrl, buildId, encodedPatchUrl))
    val connection =  url.openConnection().asInstanceOf[HttpURLConnection]
	connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
    val statusCode = connection.getResponseCode() 
    if (statusCode!= 200) {
      val response = IOUtils.toString(connection.getErrorStream())
      throw new RuntimeException(String.format("Error when notifying build server. \n using url %s\nGot response %s %S\nbody:\n%s", url.toString(), ""+statusCode, connection.getResponseMessage(), response))
    }
  }
  
  private def encode(str: String) =  {
    new String(Base64.encodeBase64(str.getBytes("UTF-8")), "UTF-8")
  }
}