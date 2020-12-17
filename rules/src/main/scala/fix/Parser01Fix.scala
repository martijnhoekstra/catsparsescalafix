package fix

import scalafix.v1._
import scala.meta._
import scala.meta.Decl.Def
import scala.meta.Type.Apply

class Parser01Fix extends SemanticRule("Parser01Fix") {
  
  val parserModuleRenames = List(
    "string1", "ignoreCase1", "oneOf1", "map1", "select1", "charsWhile1",
    "until1", "void1", "backtrack1", "as1", "unmap1", "compute1", "defer1",
    "rep1Sep", "repAs1", "rep1", "length1")
    .flatMap(oldone => List(
       oldone -> oldone.replace("1", ""),
       oldone.replace("1", "") -> oldone.replace("1", "0")
    )).toMap ++ List("flatMap10" -> "flatMap", "flatMap" -> "flatMap0").toMap
  val pClassRenames = Map(
    "orElse" -> "orElse0",
    "orElse1" -> "orElse",
    "rep" -> "rep0",
    "rep1" -> "rep",

  )

  override def fix(implicit doc: SemanticDocument): Patch = {
    var p0ClassName = "Parser0"
    var p1ClassName = "Parser"
    def pModuleName = p1ClassName

    object ParserTpeFor {
      def unapply(t: Tree): Option[String] = t match {
        case tpe @ Type.Name(_) => tpe.symbol.info match {
          case Some(si) if si.isClass => {
            if(si.toString().startsWith("cats/parse/Parser# =>")) //I have no idea what I'm doing
              Some(p0ClassName)
            else if(si.toString().startsWith("cats/parse/Parser1# =>"))
              Some(p1ClassName)
            else
              None
          }
          case _ => None
        }
        case _ => None
      }
    }

    object SemanticParserType {
      def unapply(tpe: SemanticType): Boolean = tpe match {
        case TypeRef(_, symbol, _) =>
             symbol.toString == "cats/parse/Parser1#" ||
             symbol.toString == "cats/parse/Parser#"
        case _ => false
      }
    }

    doc.tree.collect {
      case Importer(q"cats.parse", importees) => importees.collect {
        case imp @ Importee.Rename(Name("Parser"), Name(target))  =>
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
          p1ClassName = "Parser"
          Patch.replaceTree(imp, "Parser")
        }
      }.asPatch
      case sel @ Term.Select(from, t @ Term.Name(nme)) => {
        if (from.symbol.toString() == "cats/parse/Parser.")
          parserModuleRenames.get(nme).map(newName =>
            Patch.replaceTree(sel, s"$pModuleName.$newName")).getOrElse(Patch.empty)
        else
          from.symbol.info.map(_.signature).collect {
            case MethodSignature(_, _, SemanticParserType()) =>
              pClassRenames.get(nme).map(newName => Patch.replaceTree(t,newName))
          }.flatten.getOrElse(Patch.empty)
      }
      case tpe @ ParserTpeFor(to) => {
        Patch.replaceTree(tpe, to)
      }
    }.asPatch
  }
}