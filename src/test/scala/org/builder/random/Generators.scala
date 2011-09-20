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

	val genFileTree = Gen.sized(sz => genDir(sz))
		
}