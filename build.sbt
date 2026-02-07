import extras.scala.io.syntax.color._

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / organization := props.Org
ThisBuild / organizationName := "Kevin's Code"

ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  )
)

ThisBuild / licenses := props.licenses

lazy val root = (project in file("."))
  .settings(
    name := props.RepoName,
    scalacOptions := scalacOptions.value.distinct,
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
    scalacOptions := scalacOptions.value.distinct,
    libraryDependencies ++= {

      val latestVersion = DocsTools.getTheLatestTaggedVersion(props.GitHubUsername, props.CodeRepoName)(println)

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
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
    mdocVariables := {
      implicit val logger: Logger = sLog.value

      val latestVersion = DocsTools.getTheLatestTaggedVersion(props.GitHubUsername, props.CodeRepoName)(logger.error(_))
      DocsTools.createMdocVariables(latestVersion)
    },
    mdoc := {
      implicit val logger: Logger = sLog.value

      val latestVersion = DocsTools.getTheLatestTaggedVersion(props.GitHubUsername, props.CodeRepoName)(logger.error(_))

      val envVarCi = sys.env.get("CI")
      val ciResult = s"""sys.env.get("CI")=${envVarCi}"""
      envVarCi match {
        case Some("true") =>
          logger.info(
            s">> ${ciResult.yellow} so ${"run".green} `${"writeLatestVersion".blue}` and `${"writeVersionsArchived".blue}`."
          )
          val websiteDir = docusaurDir.value
          DocsTools.writeLatestVersion(websiteDir, latestVersion)
          DocsTools.writeVersionsArchived(props.GitHubUsername, props.CodeRepoName)(websiteDir, latestVersion)(logger)
        case Some(_) | None =>
          logger.info(
            s">> ${ciResult.yellow} so it will ${"not run".red} `${"writeLatestVersion".cyan}` and `${"writeVersionsArchived".cyan}`.\n" +
              s">> If you want to write these files locally, run sbt with ${"CI=true".yellow}.\n" +
              s">> e.g.) ${"CI=true".blue} ${"sbt".blue}"
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
    scalacOptions := scalacOptions.value.distinct,
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
    mdocVariables := DocsTools.createMdocVariables("0.19.0"),
  )
  .settings(noPublish)

lazy val props =
  new {

    private val GitHubRepo = findRepoOrgAndName

    val Org = "io.kevinlee"

    val GitHubUsername = GitHubRepo.fold("kevin-lee")(_.orgToString)
    val RepoName       = GitHubRepo.fold("refined4s-docs")(_.nameToString)

    val CodeRepoName = RepoName.stripSuffix("-docs")

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
    val HedgehogExtraVersion = "0.20.0"

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
