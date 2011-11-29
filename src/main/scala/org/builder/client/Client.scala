package org.builder.client
import java.io.File
import org.builder.versioncontrol.git.commandline.Git
import org.builder.server.api.ServerApi
import java.util.regex.Pattern
import org.builder. util.BuildId
import org.builder.versioncontrol.VersionControl
import org.builder.versioncontrol.Patch
import org.builder.util.Properties
import org.builder.versioncontrol.VCType._

class Client() {

  private def createPatch(vc: VersionControl, file: File): Patch  = {
    if (checkForUntrackedFiles(vc)) {
		return null
	}
	if (!vc.hasChanges()) {
		println("No changes found...")
		return null
	}
	val patch = vc.createPatch(file)
	if (file.length() == 0) {
		println("No changes found...")
		return null
	}
	patch
  }

	def build(vc: VersionControl, serverUrl: String, ciUrl: String, jobName: String): Boolean = {
	  val patchFile = new File(vc.dir, "builder-patch.txt")
	  val patch = createPatch(vc, patchFile)
	  if (patch != null) {
		  val server = new ServerApi()
		  val buildResource = server.send(serverUrl, patch, ciUrl, jobName, vc.originUrl, vc.vcType)
		  println(String.format("Successfully send build request. Follow it at %s", buildResource))
		  true
	  } else false
		
		
//		val pattern = Pattern.compile(".*\\?buildid=(.*+)")
//		val matcher = pattern.matcher(buildResource)
//		matcher.matches()
//		val buildId = matcher.group(1)
		
	}

	  

	def applyPatch(patchUrl: String, repoUrl: String, vcType: VCType, dir: File) {
	  
	  val buildId = BuildId.fromPatchUrl(patchUrl)
      
	  val vc = VersionControl.getVc(vcType, dir)
      
	  val revision = BuildId.getRevisionFromId(buildId)			
	  
	  if (repoUrl != null) vc.cloneAndCheckout(repoUrl, revision)
		
		var patchFile: File = null
		try {
			patchFile =  new File(vc.dir, buildId + ".patch")
			new ServerApi().fetchToFile(patchUrl, patchFile)
			vc.apply(patchFile)
		} finally {
			if (patchFile != null) patchFile.delete()
		}
	}


	def checkForUntrackedFiles(vc: VersionControl): Boolean =  {
		val q = "There are files that are not tracked by your version control. These files will not be send to the build server. Continue?"
		!vc.untrackedFiles().isEmpty && !new CommandLine().yesNo(q) 
	}

}