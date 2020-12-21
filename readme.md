# Scalafix to help migrating to cats-parse 0.3.x

This scalafix is intended to help migrating from cats-parse 0.2.x to
cats-parse 0.3.x, which switches around the Parser1/Parser distinction
to a Parser/Parser0 distinction and renames many methods that had a variety
with 1 and a normal one to having a normal one and one with 0 respectively.

tl;dr:

add the scalafix plugin:

```
// in project/plugins.sbt
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.24")
```

Then run the scalafix from the sbt console

```
scalafixEnable
Test / scalafix github:martijnhoekstra/catsparsescalafix/Parser01Fix
scalafix github:martijnhoekstra/catsparsescalafix/Parser01Fix
```

Then update to cats-parser 0.3.x and cleanup the missing bits manually.

## Migrate tests first

This fix needs your project to be able to compile to run. If you fix your main
project, your test will no longer compile, and can't be scalafixed.
## Bits you may have to clean up manually if you're unlucky

* Some instance methods aren't replaced properly -- seemingly when an instance
  method got replaced earlier in the same expression. This will tend to show itself
  as "orElse1 not found". Manually change it to `orElse`.

* When you only used `Parser` as a module to use the constructors from,
  it will be renamed to `Parser0`, which will be unused. You can fix this by
  running the built-in rule RemoveUnused with `scalafix RemoveUnused` in the sbt
  console

* If you hadn't imported `Parser1`, only `Parser` (under a rename or not) then
  after the fix, `Parser` itself will no longer be imported. Manually add it
  to fix.

* When you imported `Parser1` before without renaming and also have your own
  type `Parser`, the import will be renamed to `Parser` and shadow the one in
  scope. Manually fix by either
  * renaming the `Parser1` import before running the scalafix to e.g. `P1` (most
    popular choice)
  * removing the import and using the fully qualified name `cats.parse.Parser`
  * using the fully qualified name of your own type.

* When you had `Parser.rep(inner)` where `inner` is some other tree that has
  a replacement, then you'll end up with scrambled output. Either Factor out
  `inner` into a val first, or manually clean up after.

* Other bits: if you had more bits you had to clean up manually, please report
  an issue so it can be added, or make a PR to add it yourself. Maybe we can fix
  the scalafix too.