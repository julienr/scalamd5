//http://lwjgl.org/forum/index.php?topic=3416.0
import sbt._
import java.io.File

// simple build tool does not find the so libraries automatically, 
// so we need to import them manually 

class LWJGLProject(info: ProjectInfo) extends DefaultProject(info) with de.tuxed.codefellow.plugin.CodeFellowPlugin {
  val scalatest = "org.scalatest"%"scalatest"%"1.3"


  // to specify new runJVMOptions we need to fork the execution    
  override def fork = Some(new ForkScalaRun {
    val (os, separator) = System.getProperty("os.name").split(" ")(0).toLowerCase match {
      case "linux" => "linux" -> ":"
      case "mac" => "macosx" -> ":"
      case "windows" => "windows" -> ";"
      case "sunos" => "solaris" -> ":"
      case x => x -> ":"
    }

    override def runJVMOptions = super.runJVMOptions ++ Seq("-Djava.library.path=" + System.getProperty("java.library.path") + separator + ("lib" / "native" / os))
    override def scalaJars = Seq(buildLibraryJar.asFile, buildCompilerJar.asFile)

  })
}
