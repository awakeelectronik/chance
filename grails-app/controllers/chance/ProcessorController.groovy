package chance
import org.scrapper.*

class ProcessorController {
    def scrapperService
    def index() { 
        scrapperService.scrappingFromLastDate()

        int tmp, before = 1, actual = 1
        double[][] repetitionDigit = new double[4][10]
        double[][] repetitionTwoDigit = new double[6][100]
        double[][] repetitionThreeDigit = new double[4][1000]
        double[] repetitionFourDigit = new double[10000]
        Calendar cal = Calendar.getInstance()

        int[] bestNumber = new int[100]
        double[] best = new double[100]
        for(int a=0;a<100;a++){
            best[a]=10000.0
            bestNumber[a]=1000-a
        }

        while(actual<5000){
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -actual-7);
            
            repetitionDigit = queryDigit(repetitionDigit, cal)
            repetitionTwoDigit = queryTwoDigits(repetitionTwoDigit, cal)
            repetitionThreeDigit = queryThreeDigits(repetitionThreeDigit, cal)
            repetitionFourDigit = queryFourDigits(repetitionFourDigit, cal)

            tmp = before
            before = actual
            actual += tmp
        }
        LinkedHashMap<Integer, Double> resultsAnalized = new LinkedHashMap<>();
        repetitionFourDigit.eachWithIndex { result, k ->
            result += calculateProbabilityResult(k, repetitionDigit, repetitionTwoDigit, repetitionThreeDigit, repetitionFourDigit)
            // print "key: " + k + "; prob: " + result

            if(best[99]>result){
                resultsAnalized.remove(bestNumber[99])
                resultsAnalized.put(k,result)
                best[99] = result
                bestNumber[99] = k
                Arrays.sort(best)
                best.eachWithIndex { value, ki ->
                    Iterator<Integer, Double> savedIt = resultsAnalized.iterator()

                    while (savedIt.hasNext()) {
                        def key = savedIt.next();
                        Double comp1 = key.value

                        if (comp1.equals(value))
                            bestNumber[ki]=key.key
                    }
                }
            }
        }
        
        render bestNumber
    }

    private double calculateProbabilityResult(int number, double[][] repetitionDigit, double[][] repetitionTwoDigit, double[][] repetitionThreeDigit, double[] repetitionFourDigit){
        double tmp = 0.0
        String numberString
        if(number<10)
            numberString = "000"+number
        else if(number<100)
            numberString = "00"+number
        else if(number<1000)
            numberString = "0"+number
        else
            numberString = String.valueOf(number)

        // probabilidad por dígito
        tmp += repetitionDigit[0][Integer.parseInt(numberString[0])]
        tmp += repetitionDigit[1][Integer.parseInt(numberString[1])]
        tmp += repetitionDigit[2][Integer.parseInt(numberString[2])]
        tmp += repetitionDigit[3][Integer.parseInt(numberString[3])]
        // print "Primer digito: " + tmp

        // probabilidad por dos dígitos
        tmp += repetitionTwoDigit[0][Integer.parseInt(numberString[0]+numberString[1])]
        tmp += repetitionTwoDigit[1][Integer.parseInt(numberString[1]+numberString[2])]
        tmp += repetitionTwoDigit[2][Integer.parseInt(numberString[2]+numberString[3])]
        tmp += repetitionTwoDigit[3][Integer.parseInt(numberString[0]+numberString[2])]
        tmp += repetitionTwoDigit[4][Integer.parseInt(numberString[0]+numberString[3])]
        tmp += repetitionTwoDigit[5][Integer.parseInt(numberString[1]+numberString[3])]
        // print "Uno y dos digitos: " + tmp

        // probabilidad con tres dígitos
        tmp += repetitionThreeDigit[0][Integer.parseInt(numberString[0]+numberString[1]+numberString[2])]
        tmp += repetitionThreeDigit[1][Integer.parseInt(numberString[1]+numberString[2]+numberString[3])]
        tmp += repetitionThreeDigit[2][Integer.parseInt(numberString[2]+numberString[3]+numberString[0])]
        tmp += repetitionThreeDigit[3][Integer.parseInt(numberString[1]+numberString[3]+numberString[0])]
        // print "Uno, dos y tres digitos: " + tmp

        return tmp
    }

    private double[][] queryDigit(double[][] prob, Calendar cal){
        def resultsBetween = Result.executeQuery('SELECT COUNT(first) AS repetition, first FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY first',['%d/%m/%Y',cal.time])
        prob[0] = sumProbDigit(resultsBetween, prob[0], 1.0)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(second) AS repetition, second FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY second',['%d/%m/%Y',cal.time])
        prob[1] = sumProbDigit(resultsBetween, prob[1], 1.0)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(third) AS repetition, third FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY third',['%d/%m/%Y',cal.time])
        prob[2] = sumProbDigit(resultsBetween, prob[2], 1.2)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(fourth) AS repetition, fourth FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY fourth',['%d/%m/%Y',cal.time])
        prob[3] = sumProbDigit(resultsBetween, prob[3], 1.2)

        return prob
    }

    private double[][] queryTwoDigits(double[][] prob, Calendar cal){
        def resultsBetween = Result.executeQuery('SELECT COUNT(first) AS repetition, first, second FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY first, second',['%d/%m/%Y',cal.time])
        prob[0] = sumProbTwoDigit(resultsBetween, prob[0], 1.0)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(second) AS repetition, second, third FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY second, third',['%d/%m/%Y',cal.time])
        prob[1] = sumProbTwoDigit(resultsBetween, prob[1], 1.05)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(third) AS repetition, third, fourth FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY third, fourth',['%d/%m/%Y',cal.time])
        prob[2] = sumProbTwoDigit(resultsBetween, prob[2], 1.2)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(third) AS repetition, third, first FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY third, first',['%d/%m/%Y',cal.time])
        prob[3] = sumProbTwoDigit(resultsBetween, prob[3], 1.05)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(first) AS repetition, first, fourth FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY first, fourth',['%d/%m/%Y',cal.time])
        prob[4] = sumProbTwoDigit(resultsBetween, prob[4], 1.05)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(fourth) AS repetition, fourth, second FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY fourth, second',['%d/%m/%Y',cal.time])
        prob[5] = sumProbTwoDigit(resultsBetween, prob[5], 1.05)
        
        return prob
    }

    private double[][] queryThreeDigits(double[][] prob, Calendar cal){
        def resultsBetween = Result.executeQuery('SELECT COUNT(first) AS repetition, first, second, third FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY first, second, third',['%d/%m/%Y',cal.time])
        prob[0] = sumProbThreeDigit(resultsBetween, prob[0], 1.0)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(second) AS repetition, second, third, fourth FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY second, third, fourth',['%d/%m/%Y',cal.time])
        prob[1] = sumProbThreeDigit(resultsBetween, prob[1], 1.2)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(third) AS repetition, third, fourth, first FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY third, fourth, first',['%d/%m/%Y',cal.time])
        prob[2] = sumProbThreeDigit(resultsBetween, prob[2], 1.2)
        
        resultsBetween = Result.executeQuery('SELECT COUNT(second) AS repetition, second, fourth, first FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY second, fourth, first',['%d/%m/%Y',cal.time])
        prob[3] = sumProbThreeDigit(resultsBetween, prob[3], 1.0)
        
        return prob
    }
 
    private double[] queryFourDigits(double[] prob, Calendar cal){
        def resultsBetween = Result.executeQuery('SELECT COUNT(first) AS repetition, first, second, third, fourth FROM Result ' +
                                                    'WHERE STR_TO_DATE(date,?)> ?' +
                                                    'GROUP BY first, second, third, fourth',['%d/%m/%Y',cal.time])
        int totalResults = 0
        resultsBetween.each { p->
            totalResults += p[0]
        }
        resultsBetween.each { p->
            prob[Integer.parseInt(p[1]+""+p[2]+p[3]+p[4])] += (p[0]/totalResults)*10000
        }
        return prob
    }

    private double[] sumProbThreeDigit(results, double[] prob, double factor){
        double[] tmp = prob
        int totalResults = 0
        
        results.each { p->
            totalResults += p[0]
        }
        results.each { p->
            tmp[Integer.parseInt(p[1]+""+p[2]+p[3])] += ((p[0]/totalResults)*100*factor)
        }
        return tmp
    }

    private double[] sumProbTwoDigit(results, double[] prob, double factor){
        double[] tmp = prob
        int totalResults = 0
        
        results.each { p->
            //print p[0] + " - " + p[1]
            totalResults += p[0]
        }
        results.each { p->
            tmp[Integer.parseInt(p[1]+""+p[2])] += ((p[0]/totalResults)*3*factor)
        }
        return tmp
    }

    private double[] sumProbDigit(results, double[] prob, double factor){
        double[] tmp = prob
        int totalResults = 0
        
        results.each { p->
            //print p[0] + " - " + p[1]
            totalResults += p[0]
        }
        results.each { p->
            tmp[Integer.parseInt(p[1])] += (p[0]/totalResults)*factor
        }
        return tmp
    }
}
