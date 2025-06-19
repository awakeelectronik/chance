package chance
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import java.text.SimpleDateFormat
import org.scrapper.*


class ScrapperController {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    RestBuilder rest = new RestBuilder()
    def scrapperService

    def index() {
        String message = scrapperService.scrappingFromLastDate()
        render message
    }



    def fixSign() {
        int a = 0
        Result.all.each { result ->
            if(result.sign.contains("Z")){
                String tmp = getResultREST(result.date)
                String resp = cleanData(tmp)
                result.sign =  resp[4]
                result.save(flush:true)
                a++
            }
        }
        render a
        print a
    }
 
    def fromSpecificDate() {
        String date = "17/06/2025"
        String message = ""

        for(int a=0;a<5;a++){
            String tmp = getResultREST(date)
            if(tmp!='<div class="displayN"><div id="resultadosTabla"><table class="tbSecos"><tbody><tr><td colspan="3"><b>No se han encontrado resultados de la fecha seleccionada</b></td></tr></tbody></table></div></div>'){
                String result = cleanData(tmp)
                Result resultToSave = new Result(first: result[0], second: result[1], third: result[2], fourth: result[3], sign: result[4], date: date)
                resultToSave.save()
                message += a + ": " + result
            }
            
            Result.withSession { session ->
                session.flush()
            }
            date = generateNextDay(date)
        }
        render message
    }

    private String cleanData(String result){
        int index = result.indexOf("---")
        int indexEndSign = result.indexOf("<", index+4)
        String number = result.substring(index-4,index)
        String sign = result.substring(index+3,indexEndSign).replace("-","").toLowerCase()
        String letterSign
        switch (sign) {
            case "acuario":  
                letterSign = "A";
                break;
            case "acurio":  
                letterSign = "A";
                break;
            case "piscis":  
                letterSign = "B";
                break;
            case "aries":  
                letterSign = "C";
                break;
            case "tauro":  
                letterSign = "D";
                break;
            case "geminis":  
                letterSign = "E";
                break;
            case "géminis":  
                letterSign = "E";
                break;
            case "cancer":  
                letterSign = "F";
                break;
            case "cáncer":  
                letterSign = "F";
                break;
            case "leo":  
                letterSign = "G";
                break;
            case "virgo":  
                letterSign = "H";
                break;
            case "libra":  
                letterSign = "I";
                break;
            case "escorpion":  
                letterSign = "J";
                break;
            case "escorpio":  
                letterSign = "J";
                break;
            case "escorpión":  
                letterSign = "J";
                break;
            case "sagitario":  
                letterSign = "K";
                break;
            case "capricornio":  
                letterSign = "L";
                break;
            default: letterSign = "Z";
                break;
        }
        return number + letterSign
    }

    private String getResultREST(String plainDate){
        RestResponse resp = rest.get("https://resultadodelaloteria.com/ws/services.asmx/getResultado?sFecha="+plainDate+"&idLoteria=21&valueCaptcha=kZyAcju1QZE5sNoRHMohIg==&txtValueCaptcha=DMNT") {}
        return resp.body 
    }

    private String generateNextDay(String oldDateShort){
        Date oldDate = formatter.parse(oldDateShort);
        Date newDate = new Date(oldDate.getTime()+(1000*24*60*60))  

        return formatter.format(newDate);
    }

    def createResult(){
        Result result = new Result(result: "1345A", date: "12/01/2009")
        result.save(flush: true)
        print result
    }
}