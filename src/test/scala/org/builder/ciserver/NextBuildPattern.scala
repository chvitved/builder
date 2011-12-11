package org.builder.ciserver
import org.junit.Test
import org.junit.Assert._
import java.util.regex.Pattern

@Test
class NextBuildPattern {

  @Test
  def testNextBuildPattern() {
    val str = """{"number":41,"url":"http://riak04:8080/job/medicinkortet_trunk_builder/41/"},"nextBuildNumber":42,"property":[{"parameterDefinitions":[{"defaultParameterValue":"""
    //val pattern = Pattern.compile(""".*\"nextBuildNumber\"\s*:\s*((\\d+))\s*""")
    val pattern = Pattern.compile(""""nextBuildNumber"\s*:\s*(\d+)""")
    val matcher = pattern.matcher(str)
    assertTrue(matcher.find)
    val buildId = matcher.group(1)
    println(buildId)
  }
}