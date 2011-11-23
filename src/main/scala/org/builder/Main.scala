package org.builder
import java.util.regex.Pattern
import org.builder.server.impl.Server
import org.builder.server.api.ServerApi
import org.builder.client.Client
import java.io.File
import java.io.FileReader
import org.builder.util.Properties
import org.builder.versioncontrol.git.commandline.Git
import org.apache.commons.io.FileUtils
import org.builder.command.Command
import org.builder.versioncontrol.VersionControl
import org.builder.versioncontrol.svn.commandline.Svn


object Main {

	def main(args : Array[String]) {
		if ("server".equals(args(0))) {
			new Server().start
		} else if ("test".equals(args(0))) {
		  Command.execute("env")(new File("."))
		  Command.execute("git")(new File("."))
		} else if ("build".equals(args(0))) {
			val dir = new File(".")
			val properties = new Properties(".builder-properties")
			val serverUrl = properties.readProperty("server.url")
			val ciJobUrl = properties.readProperty("ci.job.url")
			val server = new ServerApi(serverUrl)
			
			val vc: VersionControl = new Svn(new File("."), null)
			new Client(vc, server).build(ciJobUrl)
						
		}else if ("applyPatch".equals(args(0))) {
			new Client(null, null).applyPatch(args(1))
		} 
	}
}
