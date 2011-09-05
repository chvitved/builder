package org.builder.util
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date

object BuildId {
	
	val formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
	
	def getRevisionFromId(buildId: String) = {
		buildId.split("_")(1)
	}
	
	def createBuildId(projectName: String, revision: String) = {
		String.format("%s_%s_%s", projectName, revision, formatter.format(new Date())) 
	}

}