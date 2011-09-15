package org.builder.random
import org.scalacheck.Gen
import java.io.File
import org.apache.commons.io.FileUtils
import org.scalacheck.Arbitrary
import org.scalacheck.util.Buildable

object Generators {

	val genNames = Gen.alphaStr suchThat (_.length > 2)
	val genFile = genNames map (FileLeaf(_))

	def genDir(sz: Int): Gen[FileTree] = {
		if (sz <= 0) genFile
		else {
			val children = Gen.listOfN(5, Gen.frequency((4, genFile), (1, genDir(sz - 5))))
			for{
				c <- children
				name <- genNames
			} yield Directory(name, c)
		}
	}

	val genFileTree = Gen.sized(sz => genDir(sz))
	
	//val originDir = new Directory("origin", List())
	//val randomTestsDir = new Directory("randomtests", List(originDir))
	
}