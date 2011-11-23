package org.builder.server.impl

import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.ServletHolder
import org.builder.util.Properties
import org.builder.ciserver.CIServerApi;

class Server {
  
  var server: org.mortbay.jetty.Server = null
  
  def start: Server = {
	start(new CIServerApi(), 7000)
  }
	
  def stop() {
	if (server != null) {
	  server.stop
	}
  }
	
  def start(buildServer: CIServerApi, port: Integer): Server = {
    stop();
    server = new org.mortbay.jetty.Server(port)
    val root = new Context(server,"/",Context.SESSIONS)
    root.setMaxFormContentSize(1024 * 1024 * 1024) //one gigabyte
    root.addServlet(new ServletHolder(new RecieveServlet(buildServer)), "/build/*")
    server.start()
    this
  }
}