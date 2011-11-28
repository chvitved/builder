package org.builder.util
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import org.builder.versioncontrol.VCType._

object BuildId {
	
	val formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS")
	
	def getRevisionFromId(buildId: String) = {
		buildId.split("_")(1)
	}
	
	def createBuildId(projectName: String, revision: String, vc: VCType) = {
		String.format("%s_%s_%s_%s", projectName, revision, vc.toString, formatter.format(new Date())) 
	}

}