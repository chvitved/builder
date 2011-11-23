package org.builder.versioncontrol
import java.io.File

abstract class VersionControl {

  def dir : File
  def createPatch(file: File) : Patch
  def getLastCommitIdFromOrigin(): String
  def checkout(revision: String)
  def apply(patchFile: File);
  def hasChanges(): Boolean
  def untrackedFiles(): Seq[String]

  def init()
  def clone(url: String)
  def clone(dir: File)
  def cloneAndCheckout(url: String, revision: String)
  def add(file: File)
  def move(src: File, dest: File)
  def remove(file: File)
  def commit(message: String)
}

case class Patch(diffFile: File, revision: String)