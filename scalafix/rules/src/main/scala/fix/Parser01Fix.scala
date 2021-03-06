package fix

import scalafix.v1._
import scala.meta._
import scala.meta.Decl.Def
import scala.meta.Type.Apply

class Parser01Fix extends SemanticRule("Parser01Fix") {

  val methodRenames = List(
    "stringX",
    "ignoreCaseX",
    "oneOfX",
    "mapX",
    "tailRecMX",
    "selectX",
    "charsWhileX",
    "untilX",
    "voidX",
    "backtrackX",
    "asX",
    "unmapX",
    "computeX",
    "deferX",
    "repXSep",
    "repAsX",
    "lengthX"
  )
    .flatMap(old =>
      List(
        old.replace("X", "1") -> old.replace("X", ""),
        old.replace("X", "") -> old.replace("X", "0")
      )
    )
    .toMap ++
    Map("flatMap" -> "flatMap0", "product" -> "product0", "softProduct" -> "softProduct0")

  val parserModuleReplacements = methodRenames.map { case (from, to) =>
    s"cats/parse/Parser.$from" -> to
  }

  object RenamedParserModuleMethodSelect {
    def unapply(t: Tree)(implicit doc: SemanticDocument): Option[String] = t match {
      case Term.Select(_, Name(_)) =>
        parserModuleReplacements.get(t.symbol.value.takeWhile(_ != '('))
      case _ => None
    }
  }

  object RenamedParserModuleMethodNoSelect {
    def unapply(t: Tree)(implicit doc: SemanticDocument): Option[(String, String)] = t match {
      case Term.Apply((Name(_), args)) =>
        val newArgs = args.mkString(",")
        for (newName <- parserModuleReplacements.get(t.symbol.value.takeWhile(_ != '(')))
          yield (newName, newArgs)
      case _ => None
    }
  }

  val pClassRenames = List(
    "orElse1" -> "orElse",
    "rep" -> "rep0",
    "rep1" -> "rep"
  ).flatMap { case (k, v) =>
    List(s"cats/parse/Parser#$k", s"cats/parse/Parser1#$k").map(_ -> v)
  }.toMap

  object RenamedParserInstanceMethod {
    def unapply(t: Tree)(implicit doc: SemanticDocument): Option[String] = t match {
      case Name(_) => pClassRenames.get(t.symbol.value.takeWhile(_ != '('))
      case _       => None
    }
  }

  object ReplacedRep {
    def unapply(tree: Tree)(implicit doc: SemanticDocument): Option[Patch] = tree match {
      case appl @ Term.Apply(fun, arg :: args) =>
        val rarg = arg match {
          case Term.ApplyInfix(_) => s"($arg)"
          case _                  => arg.toString()
        }
        val suffix = if (args.isEmpty) "" else args.mkString("(", ",", ")")
        val rep0Matcher = List("", "+1", "+2", "+3")
          .map(ov => SymbolMatcher.exact(s"cats/parse/Parser.rep($ov)."))
          .reduceLeft(_ + _)
        val repMatcher = List("", "+1", "+2", "+3")
          .map(ov => SymbolMatcher.exact(s"cats/parse/Parser.rep1($ov)."))
          .reduceLeft(_ + _)
        if (rep0Matcher.matches(fun)) {
          Some(Patch.replaceTree(appl, s"$rarg.rep0$suffix"))
        } else if (repMatcher.matches(fun)) {
          println(s"we be matching, got $rarg")
          Some(Patch.replaceTree(appl, s"$rarg.rep$suffix"))
        } else {
          None
        }
      case _ => None
    }
  }

  override def fix(implicit doc: SemanticDocument): Patch = {
    var p0ClassName = "Parser0"
    var p1ClassName = "Parser"
    def pModuleName = p1ClassName

    object ParserTpeFor {
      //I have no idea what I'm doing
      def unapply(t: Tree): Option[String] = t match {
        case tpe @ Type.Name(_) =>
          tpe.symbol.info match {
            case Some(si) if si.isClass => {
              if (si.toString().startsWith("cats/parse/Parser# =>"))
                Some(p0ClassName)
              else if (si.toString().startsWith("cats/parse/Parser1# =>"))
                Some(p1ClassName)
              else
                None
            }
            case _ => None
          }
        case _ => None
      }
    }

    doc.tree.collect {
      case Importer(q"cats.parse", importees) =>
        importees.collect {
          case imp @ Importee.Rename(Name("Parser"), Name(target)) =>
            p0ClassName = target + "0"
            Patch.replaceTree(imp, s"Parser0 => $p0ClassName")
          case imp @ Importee.Name(Name("Parser")) => {
            p0ClassName = "Parser0"
            Patch.replaceTree(imp, "Parser0")
          }
          case imp @ Importee.Rename(Name("Parser1"), Name(target)) => {
            p1ClassName = if (target.endsWith("1")) target.init else target
            Patch.replaceTree(imp, s"Parser => ${p1ClassName}")
          }
          case imp @ Importee.Name(Name("Parser1")) => {
            //if there already is a Parser imported, this clashes.
            //which is too bad, and will require manual fixing
            p1ClassName = "Parser"
            Patch.replaceTree(imp, "Parser")
          }
        }.asPatch
      case Importer(q"cats.parse.Parser", importees) =>
        importees.collect { case imp @ Importee.Name(Name(nme)) =>
          if (nme == "rep1" || nme == "rep") Patch.removeImportee(imp)
          else methodRenames.get(nme).map(Patch.replaceTree(imp, _)).getOrElse(Patch.empty)
        }.asPatch
      case ReplacedRep(patch)                       => patch
      case t @ RenamedParserInstanceMethod(newName) => Patch.replaceTree(t, newName)
      case t @ RenamedParserModuleMethodNoSelect(newName, args) =>
        Patch.replaceTree(t, s"$newName($args)")
      case t @ RenamedParserModuleMethodSelect(newName) =>
        Patch.replaceTree(t, s"$pModuleName.$newName")
      case tpe @ ParserTpeFor(to) => Patch.replaceTree(tpe, to)
    }.asPatch
  }
}
