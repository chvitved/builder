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
	    val client = new Client(repo1.vc, repo1.dir, null)
		assertFalse(client.checkForUntrackedFiles())
		val old = System.in
		System.setIn(new ByteArrayInputStream("n\n".getBytes()))
		repo1.createNewFile("newFile.txt", "i am new", false)
		assertTrue(client.checkForUntrackedFiles())
		System.setIn(old)
	  })
	}
}