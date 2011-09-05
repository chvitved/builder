
import scala.sys.process.Process

object Main {
  def main(args : Array[String]) {
    val pb = Process("""ls -alh""")
    val result: Int = pb.!
    println(result)
  }
}
