package org.builder.ciserver
import org.builder.client.Client
import org.builder.server.api.ServerApi
import org.builder.util.FileUtils
import org.builder.versioncontrol.VersionControl
import java.io.File
import org.builder.versioncontrol.VCType._

class CIServerStub(dir: File, vcType: VCType) extends CIServerApi{
	
	val client = new Client()
	
	override def build(ciUrl: String, jobName:String, repoUrl: String, patchUrl: String): String = {
		println("buildserver starting to build")
		FileUtils.deleteFile(dir)
		FileUtils.createDir(dir)
		client.applyPatch(patchUrl, repoUrl, vcType, dir)
		println("buildserver applied patch")
		"url"
	}
}