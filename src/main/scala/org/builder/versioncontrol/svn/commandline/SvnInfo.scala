package org.builder.versioncontrol.svn.commandline

import java.util.regex.Pattern

object SvnInfo {
  
  def parse(str: String): SvnInfo = {
    //why do I have to put two groups in the patterns to make it work
    val url = extractVal(str, """URL: ((.+))\n""") 
    val revision = extractVal(str, """Revision: ((\d+))\n""")
    SvnInfo(url, revision)
  }
  
  private def extractVal(str: String, pattern: String): java.lang.String = {
    val p = Pattern.compile(pattern)
    val matcher = p.matcher(str)
    println(matcher.find)
    println(matcher.groupCount)
    for(i <- 0 until matcher.groupCount) println(matcher.group(i))
    val url = matcher.group(2)
    url
  }
}

case class SvnInfo(url: String, revision: String)