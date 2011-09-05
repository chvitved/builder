package org.builder.versioncontrol
import java.io.File

abstract class VersionControl {

  def createPatch() : Patch
  def getLastCommitIdAtOriginMaster(): String
  def getLatestRevision(branch: String): String
  def init()
  def clone(repo: String)
  def add(file: File)
  def commit(message: String)
  def checkout(revision: String)
  def apply(patchFile: File);
}

case class Patch(diff: String, revision: String)