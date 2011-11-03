package org.builder.client
import java.io.File
import org.builder.versioncontrol.git.commandline.Git
import org.builder.server.api.ServerApi
import java.util.regex.Pattern
import org.builder. util.BuildId
import org.builder.versioncontrol.VersionControl

class Client(vc: VersionControl, server: ServerApi) {


	def build(projectName: String): Boolean = {
		if (checkForUntrackedFiles()) {
			return false
		}
		if (!vc.hasChanges()) {
			println("No changes found...")
			return false
		}
		val patchFile = new File(vc.dir, "builder-patch.txt")
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
		if (repoUrl != null) vc.clone(repoUrl)
		vc.checkout(revision)
		var patchFile: File = null
		try {
			patchFile =  new File(vc.dir, buildId + ".patch")
			server.fetchToFile(buildId,patchFile)
			vc.apply(patchFile)
		} finally {
			if (patchFile != null) patchFile.delete()
		}
	}

	def checkForUntrackedFiles(): Boolean =  {
		val q = "There are files that are not tracked by your version control. These files will not be send to the build server. Continue?"
		!vc.untrackedFiles().isEmpty && !new CommandLine().yesNo(q) 
	}

}