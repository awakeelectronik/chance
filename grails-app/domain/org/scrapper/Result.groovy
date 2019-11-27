package org.scrapper

class Result {
    String date
    String first
    String second
    String third
    String fourth
    String sign

    static constraints = {
    }
    
	String toString() {
		return "${first}${second}${third}${fourth}${sign} - ${date}"
	}
}
