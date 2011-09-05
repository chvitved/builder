package org.builder.buildserver
import java.net.HttpURLConnection
import java.net.URL
import org.apache.commons.io.IOUtils

class BuildserverApi(buildServerUrl: String) {
  
  def build(buildId: String, projectName: String) {
    val url = new URL(String.format("%s/job/%s/buildWithParameters?id=%s", buildServerUrl, projectName, buildId))
    val connection =  url.openConnection().asInstanceOf[HttpURLConnection]
    val statusCode = connection.getResponseCode() 
    if (statusCode!= 200) {
      val response = IOUtils.toString(connection.getErrorStream())
      throw new RuntimeException(String.format("Error when notifying build server. \n using url %s\nGot response %s %S\nbody:\n%s", url.toString(), ""+statusCode, connection.getResponseMessage(), response))
    }
  }
}