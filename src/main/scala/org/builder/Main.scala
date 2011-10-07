package org.builder
import java.util.regex.Pattern
import org.builder.server.impl.Server
import org.builder.server.api.ServerApi
import org.builder.client.Client
import java.io.File
import java.io.FileReader
import org.builder.util.Properties
import org.builder.versioncontrol.git.commandline.Git


object Main {

	def main(args : Array[String]) {
		if ("server".equals(args(0))) {
			Server.start
		} else {
			val dir = new File(".")
			val properties = new Properties(".builder-properties")
			val serverUrl = properties.readProperty("serverurl")
			
			val server = new ServerApi(serverUrl)
						
			if ("build".equals(args(0))) {
				val projectName = properties.readProperty("projectname")
				new Client(dir, server).build(projectName)
			} else if ("buildserver".equals(args(0))) {
				new Client(dir, server).applyPatch(args(1))
			} else if ("fetchfile".equals(args(0))) {
				server.fetchToFile(args(1), new File("/tmp/" + System.currentTimeMillis()))
			} else if ("patch"equals(args(0))) {
				val vc = new Git(dir)
				val p = vc.createPatch()
				println(p.diff)
				
			}
		}
	}
}
