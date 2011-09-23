package org.builder.random
import org.scalacheck.Gen
import java.io.File
import org.apache.commons.io.FileUtils
import org.scalacheck.Arbitrary
import org.scalacheck.util.Buildable

object Generators {

	//val genNames = Gen.alphaStr suchThat (c => c.length > 2 && c.length < 50 )
	val genNames = for{
		size <- Gen.choose(1,50);
		str <- Gen.containerOfN[List, Char](size, Gen.alphaChar) map (_.mkString)
	} yield str
	
	val genTextFileSize = Gen.choose(0, 50 * 1024) map (Text(_))
	val genBinaryFileSize = Gen.choose(0, 20 * 1024 * 1024) map (Binary(_))
	val genByte = Gen.choose(Byte.MinValue, Byte.MaxValue)
	
	def genTextByte : Gen[Byte] = Gen.choose(Character.MIN_VALUE,Character.MAX_VALUE) suchThat ((c) => 
		Character.isDefined(c) && !Character.isLowSurrogate(c) && !Character.isHighSurrogate(c)) map (_.getNumericValue.toByte)
	
	val genFile = for{
		name <- genNames
		fileType <- Gen.frequency((20, genTextFileSize), (1, genBinaryFileSize))
	} yield FileLeaf(name, fileType)

	def genDir(sz: Int): Gen[FileTree] = {
		val fileFrequency = 4
		val dirFrequency = 1
		val totalFrequency = fileFrequency + dirFrequency
		
		if (sz <= 0) genFile
		else {
			val children = Gen.listOfN(totalFrequency, Gen.frequency((fileFrequency, genFile), (dirFrequency, genDir(sz - totalFrequency))))
			for{
				c <- children
				name <- genNames
			} yield Directory(name, c)
		}
	}

	val genFileTree: Gen[FileTree] = Gen.sized(sz => genDir(sz))
	
	
	
	
	
	def genChange(fileTree: FileTree, parentPath: File) : Gen[Seq[Change]] = {
		fileTree match {
			case dir: Directory => {
				Gen.frequency((1, genChangeDir(dir, parentPath)), (5, genChangeDirContent(dir, parentPath)), (5,genEmptyChange))
			}
			case file: FileLeaf =>
				Gen.frequency((1, genChangeFile(file, parentPath)), (10, genEmptyChange))
		}
	}
	
	val genEmptyChange: Gen[Seq[Change]] = Gen.value(Seq())
	
	def genChangeDir(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		Gen.frequency((5, genMoveDir(dir, parentPath)), (1, genDeleteDir(dir, parentPath)))
	}
	
	def genDeleteDir(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		Gen.value(Seq(Remove(new File(parentPath, dir.name))))
	}
	
	def genMoveDir(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		//moveDir.combine(genChangeDirContent(dir, parentPath)) ((a, b) => a ++ b)
		for{
			newName <- genNames
			//mDir could be better now we rename the dir, its placed in the same parent dir
			mDir <- Gen.value(Seq(Move(new File(parentPath, dir.name), new File(parentPath, newName)))) 
			mDirContent <- genChangeDirContent(dir, parentPath)
		} yield mDir ++ mDirContent
	}
		
	
	def genChangeDirContent(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		val seq = for (child <- dir.children) yield genChange(child, new File(parentPath, dir.name))
		(seq :\ genEmptyChange) ((gen1, gen2) => 
			for {
				g1 <- gen1
				g2 <- gen2
			} yield g1 ++ g2
		)
	}
	
	def genChangeFile(file: FileLeaf, parentPath: File): Seq[Change] = {
		null
	}
	
	
	val genFilesWithChanges : Gen[(FileTree, Seq[Change])] = for {
		ft <- genFileTree
		filetreeWithChanges <- genChange(ft, null)
	} yield (ft, filetreeWithChanges)
		
}