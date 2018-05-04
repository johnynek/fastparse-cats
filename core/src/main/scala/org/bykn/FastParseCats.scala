package org.bykn.fastparse_cats

import cats.{Alternative, Eval, Functor, Monad}
import fastparse.Api
import fastparse.core
import fastparse.core.{ParserApi, ParserApiImpl}

abstract class FastParseCatsGeneric[Elem, Repr] {

  val api: Api[Elem, Repr]

  import api._
  import fastparse.core.{ParseCtx, Mutable}

  private implicit def parserApi[T, V](p: T)(implicit c: T => core.Parser[V, Elem, Repr]): ParserApi[V, Elem, Repr] =
    new ParserApiImpl[V, Elem, Repr](p)

  /**
   * From an Eval, wait to build the Parser until we run
   */
  def fromEval[A](e: Eval[Parser[A]]): Parser[A] =
    new Parser[A] {
      private[this] lazy val parser = e.value

      def parseRec(cfg: ParseCtx[Elem, Repr], index: Int): Mutable[A, Elem, Repr] =
        parser.parseRec(cfg, index)
    }

  // Mutable is of course, mutable, so this is not super safe
  private implicit val mutableFunctor: Functor[Mutable[?, Elem, Repr]] = new Functor[Mutable[?, Elem, Repr]] {
    def map[A, B](fa: Mutable[A, Elem, Repr])(fn: A => B): Mutable[B, Elem, Repr] =
      fa match {
        case Mutable.Success(a, i, t, c) =>
          Mutable.Success(fn(a), i, t, c)
        case f: Mutable.Failure[_, _] => f
        case subclass =>
          subclass.toResult match {
            case Parsed.Success(a, i) =>
              Mutable.Success(fn(a), i, subclass.traceParsers, subclass.cut)
            case Parsed.Failure(_, _, _) =>
              // this cast is safe because Failure has type Nothing
              subclass.asInstanceOf[Mutable[B, Elem, Repr]]
          }
      }
  }

  implicit val monadParser: Monad[Parser] with Alternative[Parser] = new Monad[Parser] with Alternative[Parser] {
    def empty[A]: Parser[A] = Fail
    def combineK[A](left: Parser[A], right: Parser[A]): Parser[A] = left | right

    def pure[A](a: A): Parser[A] = Pass.map(_ => a)

    override def map[A, B](fa: Parser[A])(fn: A => B): Parser[B] =
      fa.map(fn)

    override def map2Eval[A, B, C](fa: Parser[A], fb: Eval[Parser[B]])(fn: (A, B) => C): Eval[Parser[C]] =
      Eval.now(new Parser[C] {
        lazy val parserB = fb.value

        def parseRec(cfg: ParseCtx[Elem, Repr], index: Int): Mutable[C, Elem, Repr] =
          fa.parseRec(cfg, index) match {
            case Mutable.Success(a, nextIdx, _, _) =>
              mutableFunctor.map(parserB.parseRec(cfg, nextIdx))(fn(a, _))
            case f: Mutable.Failure[_, _] => f
            case subclass =>
              subclass.toResult match {
                case Parsed.Success(a, i) =>
                  mutableFunctor.map(parserB.parseRec(cfg, i))(fn(a, _))
                case Parsed.Failure(_, _, _) =>
                  // this cast is safe because Failure has type Nothing
                  subclass.asInstanceOf[Mutable[C, Elem, Repr]]
              }
          }
      })

    override def replicateA[A](cnt: Int, fa: Parser[A]): Parser[List[A]] =
      fa.rep(cnt).map(_.toList)

    override def product[A, B](fa: Parser[A], fb: Parser[B]): Parser[(A, B)] =
      fa ~ fb

    def flatMap[A, B](fa: Parser[A])(fn: A => Parser[B]): Parser[B] =
      fa.flatMap(fn)

    def tailRecM[A, B](a: A)(fn: A => Parser[Either[A, B]]): Parser[B] =
      new Parser[B] {
        def parseRec(cfg: ParseCtx[Elem, Repr], index: Int): Mutable[B, Elem, Repr] = {
          @annotation.tailrec
          def loop(a: A, idx: Int): Mutable[B, Elem, Repr] =
            fn(a).parseRec(cfg, idx) match {
              case Mutable.Success(Right(b), i, t, c) =>
                Mutable.Success(b, i, t, c)
              case Mutable.Success(Left(nexta), nextIdx, _, _) =>
                loop(nexta, nextIdx)
              case f: Mutable.Failure[_, _] => f
              case subclass =>
                subclass.toResult match {
                  case Parsed.Success(Right(b), i) =>
                    Mutable.Success(b, i, subclass.traceParsers, subclass.cut)
                  case Parsed.Success(Left(nexta), nextIdx) =>
                    loop(nexta, nextIdx)
                  case Parsed.Failure(_, _, _) =>
                    // this cast is safe because Failure has type Nothing
                    subclass.asInstanceOf[Mutable[B, Elem, Repr]]
                }
            }

          loop(a, index)
        }
      }
  }
}

object FastParseCats extends FastParseCatsGeneric[Char, String] {
  val api = fastparse.all
}
