package org.builder.server.impl

import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.ServletHolder
import org.builder.util.Properties
import org.builder.buildserver.BuildserverApi

object Server {
	
	def start {
		val properties = new Properties(".builder-server-properties")
		val buildServerUrl = properties.readProperty("buildserver.url")
		start(new BuildserverApi(buildServerUrl))
	}
	
	def start(buildServer: BuildserverApi) {
		val server = new org.mortbay.jetty.Server(7000)
		val root = new Context(server,"/",Context.SESSIONS)
		root.setMaxFormContentSize(1024 * 1024 * 1024) //one gigabyte
		root.addServlet(new ServletHolder(new RecieveServlet(buildServer)), "/build/*")
		server.start()
	}
}