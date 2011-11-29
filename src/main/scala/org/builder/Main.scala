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
import org.builder.util.UrlEncoder
import org.builder.versioncontrol.VCType
import org.builder.client.DiscoverVersionControl


object Main {

	def main(args : Array[String]) {
		if ("server".equals(args(0))) {
			new Server().start
		} else if ("test".equals(args(0))) {
		  Command.execute("env")(new File("."))
		  Command.execute("git")(new File("."))
		} else if ("build".equals(args(0))) {
			val dir = new File(".")
			val properties = new Properties(new File(dir, ".builder-properties"))
			val serverUrl = properties.readProperty("server.url")
			val ciUrl = properties.readProperty("ci.url")
			val jobName = properties.readProperty("ci.job")
			val vc: VersionControl = DiscoverVersionControl.discover(dir)
			
			new Client().build(vc, serverUrl, ciUrl, jobName)
		}else if ("applyPatch".equals(args(0))) {
		  val patchUrl = UrlEncoder.decode(args(1))
		  val vcType = VCType.fromString(args(2))
		  val repoUrl = 
		    if (args.length == 4) { 
		      UrlEncoder.decode(args(3))
		    } else null
		  
		  val dir = new File(".")
		  new Client().applyPatch(patchUrl, repoUrl, vcType, dir)
		} 
	}
}
