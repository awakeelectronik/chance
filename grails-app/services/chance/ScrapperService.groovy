// grails-app/services/chance/ResultadoScrapperService.groovy
package chance

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import java.text.SimpleDateFormat
import java.util.Calendar

import org.scrapper.*

class ScrapperService {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy")
    RestBuilder rest = new RestBuilder()

    // Método para scrapear y guardar resultados desde la última fecha hasta hoy
    def scrappingFromLastDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy")

        // Obtener la última fecha registrada
        def lastResult = Result.list(sort: "id", order: "desc", max: 1)?.first()
        Date lastDate = lastResult ? sdf.parse(lastResult.date) : sdf.parse("01/01/2020")

        // Fecha de hoy
        Date today = new Date()

        // Diferencia en días
        long diffInMillis = today.clearTime().time - lastDate.clearTime().time
        int daysToFetch = (diffInMillis / (1000 * 60 * 60 * 24))

        // Inicializar calendario
        Calendar cal = Calendar.getInstance()
        cal.setTime(lastDate)
        cal.add(Calendar.DATE, 1)

        String message = ""

        for (int i = 0; i < daysToFetch; i++) {
            String date = sdf.format(cal.time)
            String tmp = getResultREST(date)
            if (tmp != '<div class="displayN"><div id="resultadosTabla"><table class="tbSecos"><tbody><tr><td colspan="3"><b>No se han encontrado resultados de la fecha seleccionada</b></td></tr></tbody></table></div></div>') {
                String result = cleanData(tmp)
                Result resultToSave = new Result(
                    first: result[0],
                    second: result[1],
                    third: result[2],
                    fourth: result[3],
                    sign: result[4],
                    date: date
                )
                resultToSave.save()
                message += i + ": " + result + "\n"
            }
            Result.withSession { session ->
                session.flush()
            }
            cal.add(Calendar.DATE, 1)
        }
        //return message
    }


    private String cleanData(String result) {
        int index = result.indexOf("---")
        int indexEndSign = result.indexOf("<", index + 4)
        String number = result.substring(index - 4, index)
        String sign = result.substring(index + 3, indexEndSign).replace("-", "").toLowerCase()
        String letterSign
        switch (sign) {
            case "acuario":
            case "acurio":
                letterSign = "A"; break
            case "piscis":
                letterSign = "B"; break
            case "aries":
                letterSign = "C"; break
            case "tauro":
                letterSign = "D"; break
            case "geminis":
            case "géminis":
                letterSign = "E"; break
            case "cancer":
            case "cáncer":
                letterSign = "F"; break
            case "leo":
                letterSign = "G"; break
            case "virgo":
                letterSign = "H"; break
            case "libra":
                letterSign = "I"; break
            case "escorpion":
            case "escorpio":
            case "escorpión":
                letterSign = "J"; break
            case "sagitario":
                letterSign = "K"; break
            case "capricornio":
                letterSign = "L"; break
            default:
                letterSign = "Z"; break
        }
        return number + letterSign
    }

    private String getResultREST(String plainDate) {
        RestResponse resp = rest.get("https://resultadodelaloteria.com/ws/services.asmx/getResultado?sFecha=" + plainDate + "&idLoteria=21&valueCaptcha=kZyAcju1QZE5sNoRHMohIg==&txtValueCaptcha=DMNT") {}
        return resp.body
    }

}
// This service scrapes lottery results from a web service and saves them to the database.