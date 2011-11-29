package org.builder.util
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import org.builder.versioncontrol.VCType._
import java.util.regex.Pattern

object BuildId {
	
	val formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS")
	
	def getRevisionFromId(buildId: String) = {
		buildId.split("@")(1)
	}
	
	def createBuildId(projectName: String, revision: String, vc: VCType) = {
		String.format("%s@%s@%s@%s", projectName, revision, vc.toString, formatter.format(new Date())) 
	}
	
	def fromPatchUrl(patchUrl: String) : String = {
	  val pattern = Pattern.compile(""".*\?buildid=(.*+)""")
	  val matcher = pattern.matcher(patchUrl)
	  matcher.matches()
      matcher.group(1)
	}

}