//package com.example.dailytrivia.endpoints.opentdb
//
//import com.example.dailytrivia.data.model.Question
//
//class Utils {
//    fun decodeHtml(encoded: String): String {
//        return java.net.URLDecoder.decode(encoded, "UTF-8")
//    }
//
//    fun shuffleAnswers(question: Question): List<String> {
//        val allAnswers = question.incorrectAnswers + question.correctAnswer
//        return allAnswers.shuffled()
//    }
//
//}