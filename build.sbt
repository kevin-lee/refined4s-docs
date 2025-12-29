import just.semver.SemVer
import sbtcrossproject.CrossProject
import extras.scala.io.syntax.color._

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / organization := props.Org
ThisBuild / organizationName := "Kevin's Code"

ThisBuild / testFrameworks ~=
  (frameworks => (TestFramework("hedgehog.sbt.Framework") +: frameworks).distinct)

ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  )
)

ThisBuild / homepage := Some(url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}"))
ThisBuild / scmInfo :=
  Some(
    ScmInfo(
      url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}"),
      s"git@github.com:${props.GitHubUsername}/${props.RepoName}.git",
    )
  )
ThisBuild / licenses := props.licenses

ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.11"

ThisBuild / scalafixConfig := (
  if (scalaVersion.value.startsWith("3"))
    ((ThisBuild / baseDirectory).value / ".scalafix-scala3.conf").some
  else
    ((ThisBuild / baseDirectory).value / ".scalafix-scala2.conf").some
)

lazy val root = (project in file("."))
  .settings(
    name := props.RepoName
  )
  .settings(noPublish)

lazy val docs = (project in file("docs-gen-tmp/docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin)
  .settings(
    scalaVersion := props.Scala3Version,
    name := prefixedProjectName("docs"),
    mdocIn := file("docs/latest"),
    mdocOut := file("generated-docs/docs"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "generated-docs" / "docs"),
    scalacOptions ~= (ops => ops.filter(op => !op.startsWith("-Wunused:imports") && op != "-Wnonunit-statement")),
    libraryDependencies ++= {

      val latestVersion = getTheLatestTaggedVersion(println)

      List(
        "io.kevinlee" %%% "refined4s-core"          % latestVersion,
        "io.kevinlee" %%% "refined4s-cats"          % latestVersion,
        "io.kevinlee" %%% "refined4s-chimney"       % latestVersion,
        "io.kevinlee" %%% "refined4s-circe"         % latestVersion,
        "io.kevinlee" %%% "refined4s-pureconfig"    % latestVersion,
        "io.kevinlee"                              %% "refined4s-doobie-ce2" % latestVersion,
        "io.kevinlee" %%% "refined4s-extras-render" % latestVersion,
        "io.kevinlee" %%% "refined4s-tapir"         % latestVersion,
        libs.circeCore.value,
        libs.circeLiteral.value,
        libs.circeParser.value,
      )
    },
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = getTheLatestTaggedVersion(logger.error(_))
      createMdocVariables(latestVersion)
    },
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
    mdoc := {
      implicit val logger: Logger = sLog.value

      val latestVersion = getTheLatestTaggedVersion(logger.error(_))

      val envVarCi = sys.env.get("CI")
      val ciResult = s"""sys.env.get("CI")=${envVarCi}"""
      envVarCi match {
        case Some("true") =>
          logger.info(
            s">> ${ciResult.yellow} so ${"run".green} `${"writeLatestVersion".blue}` and `${"writeVersionsArchived".blue}`."
          )
          val websiteDir = docusaurDir.value
          writeLatestVersion(websiteDir, latestVersion)
          writeVersionsArchived(websiteDir, latestVersion)(logger)
        case Some(_) | None =>
          logger.info(
            s">> ${ciResult.yellow} so it will ${"not run".red} `${"writeLatestVersion".cyan}` and `${"writeVersionsArchived".cyan}`."
          )
      }
      mdoc.evaluated
    },
  )
  .settings(noPublish)

lazy val docsV0 = (project in file("docs-gen-tmp/docs-v0"))
  .enablePlugins(MdocPlugin)
  .settings(
    scalaVersion := props.Scala3Version,
    name := prefixedProjectName("docsV0"),
    mdocIn := file("docs/v0"),
    mdocOut := file("website/versioned_docs/version-v0/docs"),
    cleanFiles += ((ThisBuild / baseDirectory).value / "website" / "versioned_docs" / "version-v0"),
    scalacOptions ~= (ops => ops.filter(op => !op.startsWith("-Wunused:imports") && op != "-Wnonunit-statement")),
    libraryDependencies ++= {
      val theVersion          = "0.19.0"
      List(
        "io.kevinlee" %%% "refined4s-core"          % theVersion,
        "io.kevinlee" %%% "refined4s-cats"          % theVersion,
        "io.kevinlee" %%% "refined4s-chimney"       % theVersion,
        "io.kevinlee" %%% "refined4s-circe"         % theVersion,
        "io.kevinlee" %%% "refined4s-pureconfig"    % theVersion,
        "io.kevinlee"                              %% "refined4s-doobie-ce2" % theVersion,
        "io.kevinlee" %%% "refined4s-extras-render" % theVersion,
        "io.kevinlee" %%% "refined4s-tapir"         % theVersion,
        libs.circeCore.value,
        libs.circeLiteral.value,
        libs.circeParser.value,
      )
    },
    mdocVariables := createMdocVariables("0.19.0"),
  )
  .settings(noPublish)

lazy val CmdRun = new {
  import sys.process._

  def runAndCapture(command: Seq[String]): (Int, String, String) = {
    val out      = new StringBuilder
    val err      = new StringBuilder
    val exitCode =
      Process(command).!(
        ProcessLogger(
          (o: String) => out.append(o).append('\n'),
          (e: String) => err.append(e).append('\n'),
        )
      )
    (exitCode, out.result().trim, err.result().trim)
  }

  def fail(prefix: String, step: String, command: Seq[String], out: String, err: String)(log: String => Unit): Nothing = {
    val cmdString = command.mkString(" ")
    val details   =
      if (err.nonEmpty) err
      else if (out.nonEmpty) out
      else "(no output)"
    log(s">> [$prefix][$step] Command failed: `$cmdString`\n$details".red)
    throw new MessageOnlyException(s"$step failed: $cmdString\n$details")
  }
}

def getTheLatestTaggedVersion(logger: => String => Unit): String = {
  val (ghVersionExit, ghVersionOut, ghVersionErr) = CmdRun.runAndCapture(Seq("gh", "--version"))
  if (ghVersionExit != 0)
    CmdRun.fail(
      "getTheLatestTaggedVersion",
      "gh --version",
      Seq("gh", "--version"),
      ghVersionOut,
      ghVersionErr,
    )(logger)

  val (ghAuthExit, ghAuthOut, ghAuthErr) =
    CmdRun.runAndCapture(Seq("gh", "auth", "status", "-h", "github.com"))
  if (ghAuthExit != 0)
    CmdRun.fail(
      "getTheLatestTaggedVersion",
      "gh auth status",
      Seq("gh", "auth", "status", "-h", "github.com"),
      ghAuthOut,
      ghAuthErr,
    )(logger)

  val repo                      = "kevin-lee/refined4s"
  val tagNameCmd                =
    Seq("gh", "release", "view", "-R", repo, "--json", "tagName", "-q", ".tagName")
  val (tagExit, tagOut, tagErr) = CmdRun.runAndCapture(tagNameCmd)
  if (tagExit != 0)
    CmdRun.fail("getTheLatestTaggedVersion", "gh release view", tagNameCmd, tagOut, tagErr)(logger)

  val tagName = tagOut.trim
  if (tagName.isEmpty)
    CmdRun.fail(
      "getTheLatestTaggedVersion",
      "gh release view (empty tagName)",
      tagNameCmd,
      tagOut,
      tagErr,
    )(logger)

  if (!tagName.startsWith("v")) {
    logger(s">> [getTheLatestTaggedVersion] Expected tagName to start with 'v' but got: $tagName".red)
    throw new MessageOnlyException(s"Expected tagName to start with 'v' but got: $tagName")
  }

  val versionWithoutV = tagName.stripPrefix("v")
  SemVer.parse(versionWithoutV) match {
    case Right(v) => v.render
    case Left(parseError) =>
      logger(s">> [getTheLatestTaggedVersion] Invalid SemVer from tagName ($tagName): ${parseError.toString}".red)
      throw new MessageOnlyException(s"Invalid SemVer from tagName ($tagName): ${parseError.toString}")
  }
}

def writeLatestVersion(websiteDir: File, latestVersion: String)(implicit logger: Logger): Unit = {
  val latestVersionFile = websiteDir / "latestVersion.json"
  val latestVersionJson = raw"""{"version":"$latestVersion"}"""

  val websiteDirRelativePath =
    s"${latestVersionFile.getParentFile.getParentFile.getName.cyan}/${latestVersionFile.getParentFile.getName.yellow}"
  logger.info(
    s""">> Writing ${"the latest version".blue} to $websiteDirRelativePath/${latestVersionFile.getName.green}.
       |>> Content: ${latestVersionJson.blue}
       |""".stripMargin
  )
  IO.write(latestVersionFile, latestVersionJson)
}

def writeVersionsArchived(websiteDir: File, latestVersion: String)(implicit logger: Logger): Unit = {
  import sys.process._

  val (ghVersionExit, ghVersionOut, ghVersionErr) = CmdRun.runAndCapture(Seq("gh", "--version"))
  if (ghVersionExit != 0)
    CmdRun.fail("writeVersionsArchived", "gh --version", Seq("gh", "--version"), ghVersionOut, ghVersionErr)(logger.error(_))

  val (ghAuthExit, ghAuthOut, ghAuthErr) =
    CmdRun.runAndCapture(Seq("gh", "auth", "status", "-h", "github.com"))
  if (ghAuthExit != 0)
    CmdRun.fail(
      "writeVersionsArchived",
      "gh auth status",
      Seq("gh", "auth", "status", "-h", "github.com"),
      ghAuthOut,
      ghAuthErr,
    )(logger.error(_))

  val repo      = "kevin-lee/refined4s"
  val ghTagsCmd =
    Seq(
      "gh",
      "api",
      "-H",
      "Accept: application/vnd.github+json",
      s"/repos/$repo/tags",
      "--paginate",
      "-q",
      ".[].name",
    )

  val (tagsExit, tagsOut, tagsErr) = CmdRun.runAndCapture(ghTagsCmd)
  if (tagsExit != 0)
    CmdRun.fail("writeVersionsArchived", "gh api tags", ghTagsCmd, tagsOut, tagsErr)(logger.error(_))

  val tags = tagsOut.trim
  if (tags.isEmpty)
    CmdRun.fail("writeVersionsArchived", "gh api tags (empty)", ghTagsCmd, tagsOut, tagsErr)(logger.error(_))

  val versions = tags
    .split("\n")
    .map(_.trim)
    .filter(t => t.nonEmpty && t.startsWith("v"))
    .map(_.stripPrefix("v"))
    .map(SemVer.parse)
    .collect { case Right(v) => v }
    .sorted(Ordering[SemVer].reverse)
    .map(_.render)
    .filter(_ != latestVersion)

  val versionsArchivedFile = websiteDir / "src" / "pages" / "versionsArchived.json"

  val versionsInJson = versions
    .map { v =>
      raw"""  {
           |    "name": "$v",
           |    "label": "$v"
           |  }""".stripMargin
    }
    .mkString("[\n", ",\n", "\n]")

  IO.write(versionsArchivedFile, versionsInJson)
}

def createMdocVariables(version: String): Map[String, String] = {
  val versionForDoc = version

  Map(
    "VERSION" -> versionForDoc
  )
}

lazy val props =
  new {

    private val GitHubRepo = findRepoOrgAndName

    val Org = "io.kevinlee"

    val GitHubUsername = GitHubRepo.fold("kevin-lee")(_.orgToString)
    val RepoName       = GitHubRepo.fold("refined4s-docs")(_.nameToString)

    val Scala3Version = "3.3.5"

    val ProjectScalaVersion = Scala3Version

    lazy val licenses = List("MIT" -> url("http://opensource.org/licenses/MIT"))

    val removeDottyIncompatible: ModuleID => Boolean =
      m =>
        m.name == "ammonite" ||
          m.name == "kind-projector" ||
          m.name == "better-monadic-for" ||
          m.name == "mdoc"

    val IncludeTest = "compile->compile;test->test"

    val HedgehogVersion      = "0.13.0"
    val HedgehogExtraVersion = "0.15.0"

    val ExtrasVersion = "0.50.1"

    val CatsVersion = "2.13.0"

    val CirceVersion = "0.14.13"

    val PureconfigVersion = "0.17.1"

    val DoobieCe2Version = "0.13.4"
    val DoobieCe3Version = "1.0.0-RC10"

    val EmbeddedPostgresVersion = "2.0.7"

    val EffectieVersion = "2.3.0"

    val LogbackVersion = "1.5.6"

    val OrphanVersion = "0.5.0"

    val KittensVersion = "3.5.0"

    val TapirVersion = "1.11.28"

    val ChimneyVersion = "1.6.0"

    val ScalajsJavaSecurerandomVersion = "1.0.0"

    val ScalaJavaTimeVersion = "2.6.0"

    val ScalaNativeCryptoVersion = "0.2.1"

  }

lazy val libs = new {

  lazy val orphanCats = Def.setting("io.kevinlee" %%% "orphan-cats" % props.OrphanVersion)

  lazy val extrasCore           = Def.setting("io.kevinlee" %%% "extras-core" % props.ExtrasVersion)
  lazy val extrasHedgehogCirce  = Def.setting("io.kevinlee" %%% "extras-hedgehog-circe" % props.ExtrasVersion)
  lazy val extrasDoobieToolsCe2 = Def.setting("io.kevinlee" %%% "extras-doobie-tools-ce2" % props.ExtrasVersion)
  lazy val extrasDoobieToolsCe3 = Def.setting("io.kevinlee" %%% "extras-doobie-tools-ce3" % props.ExtrasVersion)
  lazy val extrasRender         = Def.setting("io.kevinlee" %%% "extras-render" % props.ExtrasVersion)

  lazy val cats = Def.setting("org.typelevel" %%% "cats-core" % props.CatsVersion)

  lazy val kittens = Def.setting("org.typelevel" %%% "kittens" % props.KittensVersion)

  lazy val circeCore    = Def.setting("io.circe" %%% "circe-core" % props.CirceVersion)
  lazy val circeParser  = Def.setting("io.circe" %%% "circe-parser" % props.CirceVersion)
  lazy val circeLiteral = Def.setting("io.circe" %%% "circe-literal" % props.CirceVersion)

  lazy val pureconfigCore    = "com.github.pureconfig" %% "pureconfig-core"    % props.PureconfigVersion
  lazy val pureconfigGeneric = "com.github.pureconfig" %% "pureconfig-generic" % props.PureconfigVersion

  lazy val doobieCoreCe2 = "org.tpolecat" %% "doobie-core" % props.DoobieCe2Version
  lazy val doobieCoreCe3 = "org.tpolecat" %% "doobie-core" % props.DoobieCe3Version

  lazy val embeddedPostgres = "io.zonky.test" % "embedded-postgres" % props.EmbeddedPostgresVersion

  lazy val effectieCore   = Def.setting("io.kevinlee" %%% "effectie-core" % props.EffectieVersion)
  lazy val effectieSyntax = Def.setting("io.kevinlee" %%% "effectie-syntax" % props.EffectieVersion)
  lazy val effectieCe2    = Def.setting("io.kevinlee" %%% "effectie-cats-effect2" % props.EffectieVersion)
  lazy val effectieCe3    = Def.setting("io.kevinlee" %%% "effectie-cats-effect3" % props.EffectieVersion)

  lazy val logback = "ch.qos.logback" % "logback-classic" % props.LogbackVersion

  lazy val tapirCore = Def.setting("com.softwaremill.sttp.tapir" %%% "tapir-core" % props.TapirVersion)

  lazy val chimney = Def.setting("io.scalaland" %%% "chimney" % props.ChimneyVersion)

  lazy val scalajsJavaSecurerandom =
    Def.setting(("org.scala-js" %%% "scalajs-java-securerandom" % props.ScalajsJavaSecurerandomVersion).cross(CrossVersion.for3Use2_13))

  lazy val tests = new {

    lazy val hedgehog = Def.setting {
      List(
        "qa.hedgehog" %%% "hedgehog-core"   % props.HedgehogVersion,
        "qa.hedgehog" %%% "hedgehog-runner" % props.HedgehogVersion,
        "qa.hedgehog" %%% "hedgehog-sbt"    % props.HedgehogVersion,
      ).map(_ % Test)
    }

    lazy val hedgehogExtraCore = Def.setting("io.kevinlee" %%% "hedgehog-extra-core" % props.HedgehogExtraVersion % Test)

    lazy val hedgehogExtraRefined4s = Def.setting("io.kevinlee" %%% "hedgehog-extra-refined4s" % props.HedgehogExtraVersion % Test)

    lazy val scalaNativeCrypto =
      Def.setting("com.github.lolgab" %%% "scala-native-crypto" % props.ScalaNativeCryptoVersion % Test)

    lazy val scalaJavaTime = Def.setting("io.github.cquiroz" %%% "scala-java-time" % props.ScalaJavaTimeVersion % Test)

  }
}

// scalafmt: off
def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// scalafmt: on

def isScala3(scalaVersion: String): Boolean = scalaVersion.startsWith("3")

def module(projectName: String, crossProject: CrossProject.Builder): CrossProject = {
  val prefixedName = prefixedProjectName(projectName)
  commonModule(prefixedName, crossProject)
}

def testModule(projectName: String, crossProject: CrossProject.Builder): CrossProject = {
  val prefixedName = s"test-${prefixedProjectName(projectName)}"
  commonModule(prefixedName, crossProject)
}

def commonModule(prefixedName: String, crossProject: CrossProject.Builder): CrossProject = {
  crossProject
    .in(file(s"modules/$prefixedName"))
    .settings(
      name := prefixedName,
      fork := true,
      semanticdbEnabled := true,
      scalafixConfig := (
        if (scalaVersion.value.startsWith("3"))
          ((ThisBuild / baseDirectory).value / ".scalafix-scala3.conf").some
        else
          ((ThisBuild / baseDirectory).value / ".scalafix-scala2.conf").some
      ),
      scalacOptions ++= (if (isScala3(scalaVersion.value)) List("-no-indent", "-explain") else List("-Xsource:3")),
//      scalacOptions ~= (ops => ops.filter(_ != "UTF-8")),
      libraryDependencies ++= libs.tests.hedgehog.value,
      wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.Nothing, Wart.ImplicitConversion, Wart.ImplicitParameter),
      Compile / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      Test / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      /* } WartRemover and scalacOptions */
      licenses := props.licenses,
      /* coverage { */
      coverageHighlighting := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) | Some((2, 11)) =>
          false
        case _ =>
          true
      }),
      /* } coverage */

//      scalacOptions ~= (_.filterNot(_.startsWith("-language"))),
//      scalacOptions ++= List(
//        "-language:dynamics",
//        "-language:existentials",
//        "-language:higherKinds",
//        "-language:reflectiveCalls",
//        "-language:experimental.macros",
//        "-language:implicitConversions",
//      ),
    )
}

lazy val jsSettingsForFuture: SettingsDefinition = List(
  Test / fork := false,
  Test / scalacOptions ++= (if (scalaVersion.value.startsWith("3")) List.empty
                            else List("-P:scalajs:nowarnGlobalExecutionContext")),
  Test / compile / scalacOptions ++= (if (scalaVersion.value.startsWith("3")) List.empty
                                      else List("-P:scalajs:nowarnGlobalExecutionContext")),
  coverageEnabled := false,
)

lazy val nativeSettings: SettingsDefinition = List(
  Test / fork := false,
  coverageEnabled := false,
)
