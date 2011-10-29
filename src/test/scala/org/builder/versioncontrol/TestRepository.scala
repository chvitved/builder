package org.builder.versioncontrol
import java.io.File

trait TestRepository {

	def dir: File
  
	def init()
	
	def hasChanges() : Boolean
	
	def untrackedFiles() : Seq[String]
	
	def clone(repo: TestRepository)
	
	def commit(msg: String)
	
	def createPatch(file: File): Patch
	
	def applyPatch(f: File)
	
	def getLastCommitAtOrigin(): String
	
	def getFileAsString(fileName: String) : String

	def createNewFile(name: String, content: String, addToVC: Boolean) 
	
	def editFile(name: String, content: String)
	
}