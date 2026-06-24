addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.9.0")

addSbtPlugin("io.kevinlee" % "sbt-docusaur" % "0.21.0")

val sbtDevOopsVersion     = "3.3.2"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-starter"   % sbtDevOopsVersion)

addSbtPlugin("org.typelevel" % "sbt-tpolecat" % "0.5.2")
