package org.scrapper

class Repetition {
    int pos
    int number
    double probability

    static constraints = {
    }

	String toString() {
		return "${pos}${number}${probability}"
	}
}
