package com.anpandu.hmm.word

import play.api.libs.json._
import scala.collection.immutable.{ Map, HashMap }

class BiGramModel(override val memory: Map[String, Int]) extends Memory(memory) {

  def count(tag: String, tag2: String): Int = {
    val index = tag + "_" + tag2
    get(index)
  }
}

object BiGramModel {

  def create(_sentences: String): BiGramModel = {
    var sentences = Json.parse(_sentences).as[List[List[List[String]]]]
    var tags = getTags(sentences)
    var memory = getMemory(sentences, tags)
    new BiGramModel(memory)
  }

  def createFromJSON(_json: String): BiGramModel = {
    var memory = Json.parse(_json).as[Map[String, Int]]
    new BiGramModel(memory)
  }

  def getTags(sentences: List[List[List[String]]]): List[String] = {
    sentences
      .flatten
      .map((token) => { token(1) })
      .distinct
  }

  def getMemory(sentences: List[List[List[String]]], tags: List[String]): HashMap[String, Int] = {
    var memory = new HashMap[String, Int]
    var new_tags = tags :+ "_START_"
    new_tags.foreach((tag) => {
      new_tags.foreach((tag2) => {
        var index = tag + "_" + tag2
        var ct = count(sentences, tag, tag2)
        if (ct > 0)
          memory += (index -> ct)
      })
    })
    memory
  }

  def count(sentences: List[List[List[String]]], tag: String, tag2: String): Int = {
    sentences
      .map((words) => {
        var n = 0
        if (tag == "_START_" && tag2 == "_START_") {
          n = 1
        } else {
          var new_words = List(List("", "_START_")) ::: words ::: List(List("", "_STOP_"))
          for (idx <- 0 to new_words.length - 2) {
            if (new_words(idx)(1) == tag && new_words(idx + 1)(1) == tag2) n += 1
          }
        }
        n
      })
      .reduce((a, b) => { a + b })
  }
}