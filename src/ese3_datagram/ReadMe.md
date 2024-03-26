Il client richiede l’orario di una certa zona
geografica (ad es. US/Alaska). 

Il server invia l’orario della zona geografica
richiesta.

Il server può calcolare l’orario in una certa zona
geografica usando il seguente codice:

Calendar myCalendar = Calendar.getInstance(TimeZone.getTimeZone("US/Alaska"));
System.out.println("Time in US/Alaska>"+myCalendar.get(Calendar.HOUR)
+":"+my.get(Calendar.MINUTE));