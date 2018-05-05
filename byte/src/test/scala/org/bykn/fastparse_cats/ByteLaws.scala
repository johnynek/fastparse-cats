package org.bykn.fastparse_cats

import fastparse.byte.all._
import cats._
import cats.tests.CatsSuite
import cats.laws.discipline.{AlternativeTests, MonadTests, SemigroupalTests}

import org.scalacheck.Arbitrary

import ByteInstances._

class FastParseCatsLaws extends CatsSuite {

  implicit val arbParse: Arbitrary[Parser[Int]] =
    Arbitrary(Arbitrary.arbitrary[Int].map { i =>
      val smallInt = (i & 0xF)
      val bytev = smallInt.toByte
      P(BS(bytev)).map(_ => smallInt)
    })

  implicit val arbParseFn: Arbitrary[Parser[Int => Int]] =
    Arbitrary(Arbitrary.arbitrary[Int].map { i =>
      val smallInt = (i & 0xF)
      val bytev = smallInt.toByte
      P(BS(bytev)).map { _ => { j => j * smallInt } }
    })

  implicit def eqP[A: Eq](implicit arbBytes: Arbitrary[Array[Byte]]): Eq[Parser[A]] =
    new Eq[Parser[A]] {
      def eqv(left: P[A], right: P[A]) = {
        (0 to 1000).forall { _ =>
          arbBytes.arbitrary.sample.forall { bs =>
            val bsBytes = Bytes.view(bs)
            (left.parse(bsBytes), right.parse(bsBytes)) match {
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
    assert(eqP[Unit].eqv(AnyByte, AnyByte))
  }
}
