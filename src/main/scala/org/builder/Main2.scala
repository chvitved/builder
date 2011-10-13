package org.builder
import java.util.regex.Pattern
import org.builder.server.impl.Server
import org.builder.server.api.ServerApi
import org.builder.client.Client
import java.io.File
import java.io.FileReader
import org.builder.util.Properties
import org.builder.versioncontrol.git.commandline.Git


object Main2 {

	def main(args : Array[String]) {
		val git = new Git(new File(".").getCanonicalFile())
	}
}
