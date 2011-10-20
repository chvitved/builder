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
		size <- Gen.choose(1,20);
		str <- Gen.containerOfN[List, Char](size, Gen.alphaChar) map (_.mkString)
	} yield str
	
	val genTextFileSize = Gen.choose(0, 10 * 1024) map (Text(_))
	val genBinaryFileSize = Gen.choose(0, 2 * 1024 * 1024) map (Binary(_))
	def genBytes(size: Int) = Gen.containerOfN[Array, Byte](size, Gen.choose(Byte.MinValue, Byte.MaxValue))
	
	def genUTFStr(size: Int) : Gen[String] =
			Gen.containerOfN[List,Char](size, Gen.choose(Character.MIN_VALUE,Character.MAX_VALUE)) map {_.filter{c=>Character.isDefined(c) && !Character.isLowSurrogate(c) && !Character.isHighSurrogate(c)}.mkString}
	
	def genChars: Gen[Char] = Gen.frequency(
			(1,Gen.choose(Character.MIN_VALUE,Character.MAX_VALUE)), 
			(1, Gen.value('\n')),
			(2, Gen.value(' ')),
			(16, Gen.alphaChar)
			
		)
	
	def genText(size: Int) : Gen[String] =
			Gen.containerOfN[List,Char](size, genChars) map {_.filter{c=>Character.isDefined(c) && !Character.isLowSurrogate(c) && !Character.isHighSurrogate(c)}.mkString}
	
	// we ignore some utf chars are longer than a byte
	def genTextBytes(size: Int): Gen[Array[Byte]] = genText(size) map (str => str.getBytes("UTF-8"))
	
	val genFile: Gen[FileLeaf] = for{
		name <- genNames
		fileType <- Gen.frequency((20, genTextFileSize), (1, genBinaryFileSize))
	} yield FileLeaf(name, fileType)

	
	def genDir(sz: Int): Gen[FileTreeRoot] = {
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
		
		def toFileTreeRoot(fileTree: FileTree): FileTreeRoot = {
		  fileTree match {
		  	case Directory(name, children) => FileTreeRoot(children)
		  	case f: FileLeaf => FileTreeRoot(Seq(f)) 
		  }
		}
		
		doGenDir().map(toFileTreeRoot _)
	}

	val genFileTree: Gen[FileTreeRoot] = Gen.sized(sz => genDir(sz)) suchThat(!_.getFiles.isEmpty)
	
	def genChange(fileTreeRoot: FileTreeRoot, parentPath: File) : Gen[Seq[Change]] = {
	  val generators = 
	    for(child <- fileTreeRoot.children) yield genChange(child, parentPath) 
		foldValues(generators)
	}
	
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
			case (file, null) => AddDir(new File(parentPath,file.getName()))
			case (file, fileType) => AddFile(new File(parentPath,file.getName), fileType)
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
	  if (dir.children.isEmpty) genEmptyChange
	  else Gen.value(Seq(Remove(new File(parentPath, dir.name))))
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
			if (length > 0)
		} yield AFileChange(position, length)
		
		Gen.value(Edit(new File(parentPath, file.name), file.fileType, changes))
	}
	
	def genDeleteFile(file: FileLeaf, parentPath: File): Gen[Change] = {
		Gen.value(Remove(new File(parentPath, file.name)))
	}
	
	def genMoveFile(file: FileLeaf, parentPath: File): Gen[Change] = {
		//TODO too simple file is just renamed. It stays in the current folder
		genNames map((name) => (Move(new File(parentPath, file.name), new File(parentPath, name))))
	}
	
	
	def genFilesWithChanges() : Gen[(FileTreeRoot, Seq[Change])] = for {
		ftr <- genFileTree
		changes <- genChange(ftr, null)
	} yield (ftr, changes)
	
}