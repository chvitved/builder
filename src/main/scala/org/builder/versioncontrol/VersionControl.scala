package org.builder.versioncontrol
import java.io.File
import org.builder.versioncontrol.git.commandline.Git
import org.builder.versioncontrol.svn.commandline.Svn
import org.builder.versioncontrol.VCType._

object VersionControl {
  
  def getVc(vcType: VCType, dir: File) : VersionControl = {
    vcType match {
      case VCType.git => new Git(dir)
      case VCType.svn => new Svn(dir)
    }
  }
}

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
  def originUrl: String
  def vcType: VCType.VCType
}

case class Patch(diffFile: File, revision: String)

