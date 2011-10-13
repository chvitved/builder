package org.builder.versioncontrol
import java.io.File

abstract class VersionControl {

  def createPatch(file: File) : Patch
  def getLastCommitIdAtOriginMaster(): String
  def getLatestRevision(branch: String): String
  def init()
  def clone(repo: String)
  def add(file: File)
  def move(src: File, dest: File)
  def remove(file: File)
  def commit(message: String)
  def checkout(revision: String)
  def apply(patchFile: File);
  def hasChanges(): Boolean
}

case class Patch(diffFile: File, revision: String)