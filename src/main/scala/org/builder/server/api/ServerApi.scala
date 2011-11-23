package org.builder.server.api
import java.net.URL
import java.net.HttpURLConnection
import java.util.zip.GZIPOutputStream
import java.io.BufferedOutputStream
import org.apache.commons.io.IOUtils
import java.util.regex.Pattern
import org.builder.versioncontrol.Patch
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.io.FileInputStream
import java.io.OutputStream
import java.io.InputStream
import java.net.URLEncoder

class ServerApi(serverUrl: String) {
	
	def send(patch: Patch, ciJobUrl: String): String = {
		var in : InputStream = null
		var out: OutputStream =  null
		try {
			val url = new URL(String.format("%s/build/?revision=%s&ciurl=%s",serverUrl, patch.revision, URLEncoder.encode(ciJobUrl, "UTF-8")))
			val connection =  url.openConnection().asInstanceOf[HttpURLConnection]
			connection.setDoOutput(true)
			connection.setRequestProperty("Content-Type", "application/x-gzip")
			out = new GZIPOutputStream(new BufferedOutputStream(connection.getOutputStream()))
			in = new FileInputStream(patch.diffFile)
			IOUtils.copy(in, out)
			out.close();
			in.close();
			val statusCode = connection.getResponseCode()
			if (statusCode != 201) {
				val response = IOUtils.toString(connection.getErrorStream())
				throw new RuntimeException(String.format("Error when sending patch to server. \nurl:%s responded with %s %s\nbody:\n%s", url.toString(), ""+statusCode, connection.getResponseMessage(), response))
			} else {
				connection.getHeaderField("Location")
			} 
		} finally {
			if (in != null) {
				in.close;				
			}
			if (out != null) {
				out.close
			}
		}
		
	}
	
	def fetchToFile(buildId: String, file: File) {
		val url = new URL(String.format("%s/build/?buildid=%s",serverUrl, buildId))
		val connection =  url.openConnection().asInstanceOf[HttpURLConnection]
		val statusCode = connection.getResponseCode()
		if (statusCode != 200) {
			throw new RuntimeException(String.format("Could not find build with id %s. Got http status code %s", buildId, "" + statusCode))
		}
		val fileStream = new BufferedOutputStream(new FileOutputStream(file))
		val inStream = new GZIPInputStream(connection.getInputStream())
		IOUtils.copy(inStream, fileStream)
		fileStream.close()	
		inStream.close()
	}

}