package org.builder.buildserver
import java.io.File
import org.builder.client.Client
import org.builder.server.api.ServerApi
import org.builder.versioncontrol.VersionControl

class BuildServerStub(vc: VersionControl, buildDir: File, repoUrl: String, serverUrl: String) extends BuildserverApi(null){
	
	val client = new Client(vc, buildDir, new ServerApi(serverUrl))
	
	override def build(buildId: String, projectName: String) {
		println("buildserver starting to build")
		client.applyPatch(buildId, repoUrl)
		println("buildserver applied patch")
	}

}