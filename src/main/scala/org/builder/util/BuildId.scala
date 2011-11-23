package org.builder.util
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date

object BuildId {
	
	val formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
	
	def getRevisionFromId(buildId: String) = {
		buildId.split("_")(0)
	}
	
	def createBuildId(revision: String) = {
		String.format("%s_%s", revision, formatter.format(new Date())) 
	}

}