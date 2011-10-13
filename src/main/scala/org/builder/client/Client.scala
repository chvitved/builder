package org.builder.client
import java.io.File
import org.builder.versioncontrol.git.commandline.Git
import org.builder.server.api.ServerApi
import java.util.regex.Pattern
import org.builder. util.BuildId

class Client(dir: File, server: ServerApi) {
	
	val vc = new Git(dir)
	
	def build(projectName: String): Boolean = {
		checkForUntrackedFiles()
		val patchFile = new File(dir, "builder-patch.txt")
		try {
			val patch = vc.createPatch(patchFile)
			if (patchFile.length() == 0) {
				println("No changes found...")
				false
			} else {
				val buildResource = server.send(patch, projectName)
				val pattern = Pattern.compile(".*\\?buildid=(.*+)")
				val matcher = pattern.matcher(buildResource)
				matcher.matches()
				val buildId = matcher.group(1)
				println(String.format("Successfully send build request with the id %s. Follow it at %s",buildId, buildResource))
				true
			}
		} finally {
			 if (patchFile.exists()) {
				 patchFile.delete();
			 }
		}
	}
	
	def applyPatch(buildId: String) {
		applyPatch(buildId, null)
	}
	
	def applyPatch(buildId: String, repoUrl: String) {
		val revision = BuildId.getRevisionFromId(buildId)			
		val vc = new Git(dir)
		if (repoUrl != null) vc.clone(repoUrl)
		vc.checkout(revision)
		var patchFile: File = null
		try {
			patchFile =  new File(dir, buildId + ".patch")
			server.fetchToFile(buildId,patchFile)
			vc.apply(patchFile)
		} finally {
			if (patchFile != null) patchFile.delete()
		}
	}

	private def checkForUntrackedFiles() {
		continue here
	}

}