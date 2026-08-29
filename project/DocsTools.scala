import just.semver.SemVer
import sbt.*
import extras.scala.io.syntax.color.*

/** @author Kevin Lee
  * @since 2026-02-07
  */
object DocsTools {

  object CmdRun {

    import sys.process.*

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

  def getTheLatestTaggedVersion(gitHubUsername: String, codeRepoName: String)(logger: => String => Unit): String =
    if (!sys.env.get("CI").flatMap(ci => scala.util.Try(ci.toBoolean).toOption).getOrElse(false)) {
      import sys.process.*
      val tag = "git rev-list --tags --max-count=1".!!.trim

      val latestTagedVersion =
        if (tag.isEmpty)
          "0.1.0-SNAPSHOT"
        else
          s"git describe --tags $tag".!!.trim.stripPrefix("v")

      println(s">> It's not CI so just getting the latest tagged version=$latestTagedVersion")
      println(s">> If you want to get the actual latest version locally, set ENV var `CI`. e.g.) `export CI=true` then run sbt, Or `CI=true sbt`")

      latestTagedVersion
    } else {
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

      val repo = s"$gitHubUsername/$codeRepoName"

      val tagNameCmd =
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

  def writeVersionsArchived(gitHubUsername: String, codeRepoName: String)(websiteDir: File, latestVersion: String)(
    implicit logger: Logger
  ): Unit = {

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

    val repo      = s"$gitHubUsername/$codeRepoName"
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
}
