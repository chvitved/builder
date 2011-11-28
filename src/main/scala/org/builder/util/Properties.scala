package org.builder.util
import java.io.FileReader
import java.io.File

class Properties(file: File) {
	
	private val properties = new java.util.Properties()
	properties.load(new FileReader(file))
	
	def readProperty(key: String, allowNull: Boolean): String = {
		val value = properties.getProperty(key)
		if (!allowNull && value == null) {
			throw new RuntimeException(String.format("no property for value %s", key));
		}
		value
	}
	
	def readProperty(key: String): String = {
		readProperty(key, false)
	}
}