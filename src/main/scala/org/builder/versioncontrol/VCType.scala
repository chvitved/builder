package org.builder.versioncontrol

object VCType extends Enumeration {
  type VCType = Value
  val git, svn = Value
  
  def fromString(str: String) : VCType = {
    withName(str)
  }
}