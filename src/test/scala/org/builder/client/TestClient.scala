package org.builder.client
import org.junit.Test
import org.junit.Assert._
import org.builder.ReposTest

@Test
class TestClient extends ReposTest{

	@Test
	def noChanges() {
		val client = new Client(repo1.dir, null)
		assertFalse(client.build("testclient"))
		repo1.createNewFile("newFile.txt", "i am new", true)
	}
}