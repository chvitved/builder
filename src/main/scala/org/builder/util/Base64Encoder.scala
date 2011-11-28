package org.builder.util
import java.net.URLEncoder
import java.net.URLDecoder

object Base64Encoder {
  
  def encode(str: String) = URLEncoder.encode(str, "UTF-8")
  
  def decode(base64Str: String) = URLDecoder.decode(base64Str, "UTF-8")

}