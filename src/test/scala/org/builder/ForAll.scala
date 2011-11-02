package org.builder
import org.builder.versioncontrol.TestRepository

object ForAll {

  def forAll(method: (TestRepository, TestRepository) => Unit) (implicit repos: Seq[(TestRepository, TestRepository)]) {
    for((origin, repo1) <- repos) method(origin, repo1)
  }
}