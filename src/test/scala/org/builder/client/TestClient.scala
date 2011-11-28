package org.builder.client
import org.junit.Test
import org.junit.Assert._
import org.builder.ReposTest
import java.io.ByteArrayInputStream
import org.builder.ForAll

@Test
class TestClient extends ReposTest{

	@Test
	def untrackedFiles() {
	  ForAll.forAll((origin, repo1) => {
	    val client = new Client()
	    assertFalse(client.build(repo1.vc, null, null, null)) // no cahnges so should not create a build
		assertFalse(client.checkForUntrackedFiles(repo1.vc))
		val old = System.in
		System.setIn(new ByteArrayInputStream("n\n".getBytes()))
		repo1.createNewFile("newFile.txt", "i am new", false)
		assertTrue(client.checkForUntrackedFiles(repo1.vc))
		System.setIn(old)
	  })
	}
}