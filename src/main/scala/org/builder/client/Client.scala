package org.builder.client
import java.io.File
import org.builder.versioncontrol.git.commandline.Git
import org.builder.server.api.ServerApi
import java.util.regex.Pattern
import org.builder. util.BuildId

class Client(dir: File, server: ServerApi) {
	
	val vc = new Git(dir)
	
	def build(projectName: String) {
		val patch = vc.createPatch()
		val buildResource = server.send(patch, projectName)
		val pattern = Pattern.compile(".*\\?buildid=(.*+)")
		val matcher = pattern.matcher(buildResource)
		matcher.matches()
		val buildId = matcher.group(1)
		println(String.format("Successfully send build request with the id %s. Follow it at %s",buildId, buildResource))			
	}
	
	def applyPatch(buildId: String) {
		val revision = BuildId.getRevisionFromId(buildId)			
		val vc = new Git(dir)
		//vc.clone(gitProjectUrl)
		vc.checkout(revision)
		val patchFile =  new File(dir, buildId + ".patch")
		server.fetchToFile(buildId,patchFile)
		vc.apply(patchFile)
	}


}