package tr2024_03_14;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class BookService {
    LinkedList<Book> books = new LinkedList<>();

    public ListBooks SearchBook(String query) { // query = "Aut-Gen-Pre"
        ListBooks result = new ListBooks();
        StringTokenizer st = new StringTokenizer(query, "-");
        String aut = null;
        String gen = null;
        float pre = 0;
        if (st.hasMoreTokens()) {
            aut = st.nextToken();
        }
        if (st.hasMoreTokens()) {
            gen = st.nextToken();
        }
        if (st.hasMoreTokens()) {
            pre = Float.parseFloat(st.nextToken());
        }
        for (Book b : books) {
            if( b.Autore.equals(aut) && b.Genere.equals(gen) && b.Prezzo<pre )
                result.list.add(b);
        }
        return result;
    }

    public boolean AddBook (Book b) {
        int count = 0;
        for (Book b1 : books) {
            if ( b1.Autore.equals(b.Autore) ) {
                if( b1.Titolo.equals(b.Titolo) )
                    return false;
                count++;
            }
        }
        if ( count < 10 ) {
            books.add(b);
            return true;
        }
        return false;
    }

}

class Book implements Serializable {
    String ISBN;
    String Titolo;
    String Autore;
    String Genere;
    Float Prezzo;
}

class ListBooks implements Serializable {
    LinkedList<Book> list = new LinkedList<>();
}