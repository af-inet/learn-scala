import scala.io.Source

object Hello extends App {

  // get current working directory
  val currentDirectory = new java.io.File(".").getCanonicalPath
  println("cd: " + currentDirectory)

  // read a file
  val lines = Source.fromFile("src/message.txt").getLines
  for (line <- lines) {
    println(line)
  }

  // GET request
  val source = Source.fromURL("https://www.example.org/").getLines.mkString("\n")
  println(source)

  // MILITARY GRADE ENCRYPTION
  val alphaLower = "abcdefghijklmnopqrstuvwxyz"
  val alphaUpper = alphaLower.toUpperCase
  val alpha = alphaLower+alphaUpper
  val offsetUpper = alphaUpper.charAt(0).toByte
  val offsetLower = alphaLower.charAt(0).toByte

  val isAlpha = (c: Char) => alpha contains c

  val rotate = (c: Byte, offset: Byte) => (c - offset + 13) % 26 + offset

  val rotateIfAlpha = (c: Char) => isAlpha(c) match {
      case true => c.isUpper match {
        case true => rotate(c.toByte, offsetUpper)
        case false => rotate(c.toByte, offsetLower)
      }
      case false => c.toByte
    }

  val rot13 = (s: String) => s
    .map(rotateIfAlpha)
    .map(_.toChar)
    .mkString

  val secretMessage = "the quick brown fox"
  println(secretMessage)
  println(rot13(secretMessage))
  println(rot13(rot13(secretMessage)))
}
