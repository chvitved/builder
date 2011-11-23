package org.builder.ciserver
import org.builder.client.Client
import org.builder.server.api.ServerApi
import org.builder.util.FileUtils
import org.builder.versioncontrol.VersionControl

class CIServerStub(vc: VersionControl, serverUrl: String) extends CIServerApi{
	
	val client = new Client(vc, new ServerApi(serverUrl))
	
	override def build(buildId: String, ciJobUrl: String, patchUrl: String, str: String) {
		println("buildserver starting to build")
		FileUtils.deleteFile(vc.dir)
		FileUtils.createDir(vc.dir)
		client.applyPatch(buildId, null)
		println("buildserver applied patch")
	}
}