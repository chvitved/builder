package org.builder.random
import org.scalacheck.Gen
import java.io.File
import org.apache.commons.io.FileUtils
import org.scalacheck.Arbitrary
import org.scalacheck.util.Buildable
import scala.util.Random

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
	
	val genFile: Gen[FileLeaf]= for{
		name <- genNames
		fileType <- Gen.frequency((20, genTextFileSize), (1, genBinaryFileSize))
	} yield FileLeaf(name, fileType)

	
	def genDir(sz: Int): Gen[FileTree] = {
		val fileFrequency = 4
		val dirFrequency = 1
		var created = 0
		
		def doGenDir(): Gen[FileTree] = {
			if ((sz - created) <= 0) genFile
			else {
				val n = Random.nextInt(10)
				created += n
				val children = Gen.listOfN(n, Gen.frequency((fileFrequency, genFile), (dirFrequency, doGenDir())))
				for{
					c <- children
					name <- genNames
				} yield Directory(name, c)
			}
		}
		doGenDir()
	}

	val genFileTree: Gen[FileTree] = Gen.sized(sz => genDir(sz))
	
	def genChange(fileTree: FileTree, parentPath: File) : Gen[Seq[Change]] = {
		fileTree match {
			case dir: Directory => {
				Gen.frequency((1, genChangeDir(dir, parentPath)), (20, genChangeDirContent(dir, parentPath)), (5, genAddToDir(dir, parentPath)), (5,genEmptyChange))
			}
			case file: FileLeaf =>
				Gen.frequency((1, genChangeFile(file, parentPath)), (10, genEmptyChange))
		}
	}
	
	val genEmptyChange: Gen[Seq[Change]] = Gen.value(Seq())

	def genAddToDir(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		val gen = for(i <- 0 to Random.nextInt(10)) 
			yield Gen.frequency((3, genAddFile(dir, parentPath)), (1, genAddDir(dir, parentPath)))
		foldValues(gen)
	}
	
	def foldValues(seq: Seq[Gen[Seq[Change]]]): Gen[Seq[Change]] = {
		(seq :\ genEmptyChange) ((gen1, gen2) => 
			for {
				g1 <- gen1
				g2 <- gen2
			} yield g1 ++ g2
		)
	}
	
	def genAddFile(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		for (f <- genFile) yield Seq(AddFile(new File(parentPath, f.name), f.fileType))
	}
	
	def genAddDir(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		for {
			tree <- genDir(Random.nextInt(5))
			fileTuple <- tree.getFiles
		} yield fileTuple match {
			case (file, null) => AddDir(file)
			case (file, fileType) => AddFile(file, fileType)
		}
	}
	
	def genChangeDirContent(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		val seq = for (child <- dir.children) yield genChange(child, new File(parentPath, dir.name))
		foldValues(seq)
	}
	
	def genChangeDir(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		Gen.frequency((1, genMoveDir(dir, parentPath)), (1, genDeleteDir(dir, parentPath)))
	}
	
	def genDeleteDir(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		Gen.value(Seq(Remove(new File(parentPath, dir.name))))
	}
	
	def genMoveDir(dir: Directory, parentPath: File) : Gen[Seq[Change]] = {
		//moveDir.combine(genChangeDirContent(dir, parentPath)) ((a, b) => a ++ b)
		for{
			newName <- genNames
			//could be better. when we rename the dir, its placed in the same parent dir
			mDir <- Gen.value(Seq(Move(new File(parentPath, dir.name), new File(parentPath, newName)))) 
			mDirContent <- genChangeDirContent(dir, parentPath)
		} yield mDir ++ mDirContent
	}
	
	
	def genChangeFile(file: FileLeaf, parentPath: File): Gen[Seq[Change]] = {
		val gen = Gen.frequency((5, genEditFile(file, parentPath)), (1, genDeleteFile(file, parentPath)), (1, genMoveFile(file, parentPath)))
		gen map((Seq(_)))
	}
	
	def genEditFile(file: FileLeaf, parentPath: File): Gen[Change] = {
		val fileSize = file.fileType.size
		val changes = for {
			change <- 0 to Random.nextInt(10)
			if (fileSize) > 0
			position = Random.nextInt(fileSize)
			length = Random.nextInt(fileSize - position)
		} yield AFileChange(position, length)
		
		Gen.value(Edit(new File(parentPath, file.name), changes))
	}
	
	def genDeleteFile(file: FileLeaf, parentPath: File): Gen[Change] = {
		Gen.value(Remove(new File(parentPath, file.name)))
	}
	
	def genMoveFile(file: FileLeaf, parentPath: File): Gen[Change] = {
		//TODO too simple file is just renamed. It stays in the current folder
		genNames map((name) => (Move(new File(parentPath, file.name), new File(parentPath, name))))
	}
	
	
	
	
	val genFilesWithChanges : Gen[(FileTree, Seq[Change])] = for {
		ft <- genFileTree
		filetreeWithChanges <- genChange(ft, null)
	} yield (ft, filetreeWithChanges)
		
}