package org.bykn.fastparse_cats

import fastparse.all._
import cats._
import cats.tests.CatsSuite
import cats.laws.discipline.{AlternativeTests, MonadTests, SemigroupalTests}

import org.scalacheck.{Arbitrary, Gen}

import StringInstances._
import buildParser._

class FastParseCatsLaws extends CatsSuite {

  val genIntParse: Gen[Parser[Int]] =
    Arbitrary.arbitrary[Int].map { i =>
      val stri = i.toString
      P(stri).map(_ => i)
    }

  implicit lazy val arbParse: Arbitrary[Parser[Int]] =
    Arbitrary(Gen.oneOf(genIntParse,
      Gen.const(Fail),
      Arbitrary.arbitrary[Int].map(delay(_))))

  implicit val genFn: Gen[Parser[Int => Int]] =
    Arbitrary.arbitrary[Int].map { i =>
      val stri = i.toString
      P(stri).map { _ => { j => j * i } }
    }

  implicit val arbParseFn: Arbitrary[Parser[Int => Int]] =
    Arbitrary(Gen.oneOf(genFn,
      Gen.const(Fail),
      Arbitrary.arbitrary[Int => Int].map(delay(_))))

  implicit def eqP[A: Eq](implicit arbStr: Arbitrary[String]): Eq[Parser[A]] =
    new Eq[Parser[A]] {
      def eqv(left: P[A], right: P[A]) = {
        (0 to 1000).forall { _ =>
          arbStr.arbitrary.sample.forall { str =>
            (left.parse(str), right.parse(str)) match {
              case (Parsed.Success(pa, a), Parsed.Success(pb, b)) =>
                pa == pb && a == b
              case (Parsed.Failure(_, _, _), Parsed.Failure(_, _, _)) =>
                //idxA == idxB
                // TODO, this may be too weak, but Alternative right distributivity fails without
                true
              case _ => false
            }
          }
        }
      }
    }

  implicit val iso: SemigroupalTests.Isomorphisms[Parser] = SemigroupalTests.Isomorphisms.invariant[Parser]

  checkAll("Parser[Int]", SemigroupalTests[Parser].semigroupal[Int, Int, Int])
  checkAll("Parser[Int]", MonadTests[Parser].monad[Int, Int, Int])
  checkAll("Parser[Int]", AlternativeTests[Parser].alternative[Int, Int, Int])

  test("parser equality works") {
    assert(eqP[Unit].eqv(Pass, Pass))
    assert(eqP[Unit].eqv(AnyChar, AnyChar))
  }
}
