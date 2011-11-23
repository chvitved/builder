package org.builder

import org.apache.http.params.HttpParams
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.params.CoreProtocolPNames
import org.apache.http.HttpVersion
import org.apache.http.util.EntityUtils
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.content.FileBody
import java.io.File
import org.apache.http.entity.mime.content.StringBody
import java.nio.charset.Charset

object SendFile {

	def main(args: Array[String]) {
		val client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		val post = new HttpPost("http://localhost:8080/job/builder/buildWithParameters");
		val entity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );

		// For File parameters
		entity.addPart("test", new FileBody(new File("wiki.txt"), "application/zip" ));

		// For usual String parameters
		//entity.addPart("revision", new StringBody("34", "text/plain", Charset.forName( "UTF-8" )));

		post.setEntity( entity );

		// Here we go!
		val response = EntityUtils.toString( client.execute( post ).getEntity(), "UTF-8" );
		println(response)
		client.getConnectionManager().shutdown();

	}

}