package ese9.gestioneCorsiWS;

import java.io.Serializable;
import java.util.HashMap;

public class GestioneCorsi {
    HashMap<String, Corso> corsi = new HashMap<>();

    class Corso implements Serializable {
        String docente;
        String nomeCorso;
        int numeroCrediti;
        int oreEsercitazione;
        int oreLezione;
        String programma;

        public Corso(String docente, String nomeCorso, int numeroCrediti, int oreEsercitazione, int oreLezione, String programma) {
            this.docente = docente;
            this.nomeCorso = nomeCorso;
            this.numeroCrediti = numeroCrediti;
            this.oreEsercitazione = oreEsercitazione;
            this.oreLezione = oreLezione;
            this.programma = programma;
        }

        @Override
        public String toString() {
            return "Corso{" +
                    "docente='" + docente + '\'' +
                    ", nomeCorso='" + nomeCorso + '\'' +
                    ", numeroCrediti=" + numeroCrediti +
                    ", oreEsercitazione=" + oreEsercitazione +
                    ", oreLezione=" + oreLezione +
                    ", programma='" + programma + '\'' +
                    '}';
        }
    }

    public void aggiungiCorso(Corso corso) {
        corsi.put(corso.nomeCorso, corso);
    }

    public Corso getCorso(String nome) {
        return corsi.get(nome);
    }

}
