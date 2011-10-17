package org.builder.client
import org.junit.Test
import org.junit.Assert._
import org.builder.ReposTest
import java.io.ByteArrayInputStream

@Test
class TestClient extends ReposTest{

	@Test
	def untrackedFiles() {
		val client = new Client(repo1.dir, null)
		assertTrue(client.checkForUntrackedFiles())
		val old = System.in
		System.setIn(new ByteArrayInputStream("n\n".getBytes()))
		repo1.createNewFile("newFile.txt", "i am new", false)
		assertFalse(client.checkForUntrackedFiles())
		System.setIn(old)
	}
}