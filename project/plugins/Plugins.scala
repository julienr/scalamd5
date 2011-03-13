import sbt._
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val codefellow = "de.tuxed" % "codefellow-plugin" % "0.3"
}

// vim: set ts=2 sw=2 et:
