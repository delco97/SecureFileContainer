import javax.security.auth.login.CredentialException;
import java.io.Serializable;
import java.util.Iterator;

public interface ISecureFileContainer<E extends SecureFile> extends Serializable {
    /*
     * Overview:
     * ISecureFileContainer<E> è  un  contenitore  di oggetti  di  tipo  E.  Intuitivamente  la collezione  si  comporta
     * come una specie File Storage per la memorizzazione e condivisione di file. La collezione deve garantire un
     * meccanismo di sicurezza dei  file fornendo un proprio  meccanismo di  gestione delle identità degli utenti.
     * Ogni file  ha  un  proprietario  che ha  diritto  a  leggere,  scrivere  e fare  una  copia. La  collezione
     * deve,  inoltre, fornire un meccanismo di controllo degli accessi che permette al proprietario del file di
     * eseguire una restrizione  selettiva  dell'accesso  ai  suoi  file inseriti  nella  collezione.  Alcuni  utenti
     * possono  essere  autorizzatidal proprietario ad accedere ai suoi file (in solo lettura o anche scrittura) mentre
     * altri non possono accedervi  senza  autorizzazione. Ma  l’utente  deve  accettare  la  condivisione previa
     * autenticazione.
     *
     *  Typical Element:
     *  Un tipico ISecureFileContainer<E> è costituito dai seguenti elementi:
     *      U = {u0,u1,...,un-1} è un insieme di n utenti.
     *          ui = {id, password}
     *
     *      D = {d0,d1,...,dm-1} è un insieme di m elementi di tipo E.
     *
     *      A = {r,w,u} insieme dei livelli di accesso ai dati:
     *         - u: undefined
     *         - r: read
     *         - w: write
     *
     *      Owner: D -> U
     *         E' una funzione totale che associa a ciascun elemento d di D un elemento di U.
     *         Dati u di U, d di D Owner(d) = u ha il seguente significato:
     *         Il proprietario di d è l'utente u
     *
     *      Access: U * D -> A
     *         E' una funzione totale che associa ad ogni coppia (u,d) di U * D un elemento di A.
     *         Dati u di U, d di D, a di A Access(u,d) = a ha il seguente significato:
     *         L'utente u ha un livello di accesso pari ad a nei confronti del dato d.
     *
     * Vincoli e proprietà:
     *
     *      - Il proprietario u di un dato d ha accesso in lettura e scrittura a d:
     *        For all d di D. Owner(d) = u => Access(u,d) = w
     *      - Insieme dei dati posseduti da un utente u: OwnedData(u) = {d di D| Owner(d) = u}
     *
     */

    /*
    Crea l’identità di un nuovo utente della collezione
    @requires Id != null && passw != null && !Id.isEmpty() && !passw.isEmpty() &&
              Not (Exist u appartenente a U tale che u.id = Id)
    @throws NullPointerException se Id = null || passw = null
    @throws IllegalArgumentException se Id.isEmpty() || passw.isEmpty()
    @throws DuplicatedUserException se (Exist u appartenente a U tale che u.id = Id)
    @modifies this
    @effects u = {Id,passw} && this_post.U = this_pre.U + u
    */
    void createUser(String Id, String passw);

    /*
    Restituisce il numero dei file di un utente presenti nella
    collezione
    @requires Owner != null && passw != null && !Owner.isEmpty() && !passw.isEmpty() &&
              (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @throws NullPointerException se Owner = null || passw = null
    @throws IllegalArgumentException se Owner.isEmpty() || passw.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @return Dato u = {Owner,passw} restituisce |OwnedData(u)|
     */
    int getSize(String Owner, String passw);

    /*
    Inserisce il file nella collezione
    se vengono rispettati i controlli di identità
    @requires Owner != null && passw != null && !Owner.isEmpty() && !passw.isEmpty() && file != null &&
              (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @throws NullPointerException se Owner = null || passw = null || file = null
    @throws IllegalArgumentException se Owner.isEmpty() || passw.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw )
    @modifies this
    @effects Dato u = {Id,passw} se OwnedData(u) non contiene elementi uguali a file allora file viene inserito in
             OwnedData(u); altrimenti file non viene inserito
    @return Dato u = {Id,passw} restituisce true se file viene inserito in OwnedData(u), false altrimenti.
    */
    boolean put(String Owner, String passw, E file);

    /*
    Ottiene una copia del file nella collezione
    se vengono rispettati i controlli di identità
    @requires Owner != null && passw != null && !Owner.isEmpty() && !passw.isEmpty() && file != null &&
              (Exist (u,d) appartenente a (U * D) tale che u.id = Id && u.password = passw &&
              (Access((u,d)) = w || Access((u,d)) = r) )
    @throws NullPointerException se Id = null || passw = null || file = null
    @throws IllegalArgumentException se Owner.isEmpty() || passw.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Id && u.password = passw)
    @throws NoAccessException se (Exist u appartenente a U tale che u.id = Owner && u.password = passw) &&
                                 Not (Exist (u,d) appartenente a (U * D) tale che u.id = Id && u.password = passw &&
                                      (Access((u,d)) = w || Access((u,d)) = r) )
    @return restituisce una copia del file
    */
    E get(String Owner, String passw, E file);

    /*
    Rimuove il file nella collezione
    se vengono rispettati i controlli di identità
    @requires Owner != null && passw != null && !Owner.isEmpty() && !passw.isEmpty() && file != null
              (Exist u appartenente a U tale che u.id = Owner && u.password = passw && OwnedData(u) contiene file)
    @throws NullPointerException se Owner = null || passw = null || file = null
    @throws IllegalArgumentException se Owner.isEmpty() || passw.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @throws NoAccessException se (Exist u appartenente a U tale che u.id = Owner && u.password = passw) &&
            Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw && OwnedData(u) contiene file)
    @modifies this
    @effects this_post.D = this_pre.D - file
    @return restituisce una copia di file prima di rimuoverlo da D
     */
    E remove(String Owner, String passw, E file);

    /*
    Crea una copia del file nella collezione
    se vengono rispettati i controlli di identità
    @requires Owner != null && passw != null && !Owner.isEmpty() && !passw.isEmpty() && file != null &&
              (Exist u appartenente a U tale che u.id = Owner && u.password = passw && OwnedData(u) contiene file)
    @throws NullPointerException se Owner = null || passw = null || file = null
    @throws IllegalArgumentException se Owner.isEmpty() || passw.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @throws NoAccessException se (Exist u appartenente a U tale che u.id = Owner && u.password = passw) &&
            Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw && OwnedData(u) contiene file)
    @modifies this
    @effects effettua una copia di file
     */
    void copy(String Owner, String passw, E file);

    /*
    Condivide in lettura il file nella collezione con un altro utente
    se vengono rispettati i controlli di identità
    @requires Owner != null && passw != null && Other!=null && !Owner.isEmpty() && !passw.isEmpty() && !Other.isEmpty()
              && file != null &&
              (Exist u appartenente a U tale che u.id = Owner && u.password = passw && OwnedData(u) contiene file) &&
              (Exist u appartenente a U tale che u.id = Other)
    @throws NullPointerException se Owner = null || passw = null || Other = null || file = null
    @throws IllegalArgumentException se Owner.isEmpty() || passw.isEmpty() || Other.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @throws UnknownUserException Not (Exist u appartenente a U tale che u.id = Other)
    @throws NoAccessException se (Exist u appartenente a U tale che u.id = Owner && u.password = passw) &&
            Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw && OwnedData(u) contiene file)
    @modifies this
    @effects u= {Other, passw}, Access(u,file) = r
     */
    void shareR(String Owner, String passw, String Other, E file);

    /*
    Condivide in lettura e scrittura il file nella collezione con un altro utente
    se vengono rispettati i controlli di identità
    @requires Owner != null && passw != null && Other!=null && !Owner.isEmpty() && !passw.isEmpty() && !Other.isEmpty()
              && file != null &&
              (Exist u appartenente a U tale che u.id = Owner && u.password = passw && OwnedData(u) contiene file) &&
              (Exist u appartenente a U tale che u.id = Other)
    @throws NullPointerException se Owner = null || passw = null || Other = null || file = null
    @throws IllegalArgumentException se Owner.isEmpty() || passw.isEmpty() || Other.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @throws UnknownUserException Not (Exist u appartenente a U tale che u.id = Other)
    @throws NoAccessException se (Exist u appartenente a U tale che u.id = Owner && u.password = passw) &&
            Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw && OwnedData(u) contiene file)
    @modifies this
    @effects u= {Other, passw}, Access(u,file) = w
     */
    void shareW(String Owner, String passw, String Other, E file);

    /*
    Restituisce un iteratore (senza remove) che genera tutti i file
    dell’utente in ordine arbitrario
    se vengono rispettati i controlli di identità
    @requires Owner != null && passw != null && !Owner.isEmpty() && !passw.isEmpty() &&
              (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @throws NullPointerException se Owner = null || passw = null
    @throws IllegalArgumentException se Owner.isEmpty() || passw.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Owner && u.password = passw)
    @return Restituisce un iteratore (senza remove) che genera tutti i file dell’utente in ordine arbitrario
    */
    Iterator<E> getIterator(String Owner, String passw);

    // ****** ...altre operazione da definire a scelta *******

    /*
     Rimuove l’utente dalla collezione
     @requires Id != null && passw != null && !Id.isEmpty() && !passw.isEmpty() &&
               (Exist u appartenente a U tale che u.id = Id && u.password = passw)
     @throws NullPointerException se Id = null || passw = null
     @throws IllegalArgumentException se Id.isEmpty() || passw.isEmpty()
     @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Id && u.password = passw)
     @modifies this
     @effects u = {Id,passw} && this_post.U = this_pre.U - u &&
              this_post.D = this_pre.D - OwnedData(u)
     */
    void removeUser(String Id, String passw) throws NullPointerException, CredentialException;

    /*
    Memorizza file nel file relativo
    @requires Id != null && passw != null && !Id.isEmpty() && !passw.isEmpty() && file != null &&
              (Exist (u,d) appartenente a U tale che u.id = Id && u.password = passw && d = file && Access(u,d) = w)
    @throws NullPointerException se Id = null || passw = null || file = null
    @throws IllegalArgumentException se Id.isEmpty() || passw.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Id && u.password = passw)
    @throws NoAccessException se (Exist u appartenente a U tale che u.id = Id && u.password = passw) &&
            Not (Exist (u,d) appartenente a U tale che u.id = Id && u.password = passw && d = file && Access(u,d) = w)
    @modifies this, file
    @effects scrivi contenuto di file nel file associato
     */
    void storeFile(String Id, String passw, E file);

    /*
    Leggi file dal file relativo
    @requires Id != null && passw != null && !Id.isEmpty() && !passw.isEmpty() && file != null &&
              (Exist (u,d) appartenente a U tale che u.id = Id && u.password = passw && d = file && Access(u,d) = w)
    @throws NullPointerException se Id = null || passw = null || file = null
    @throws IllegalArgumentException se Id.isEmpty() || passw.isEmpty()
    @throws CredentialException se Not (Exist u appartenente a U tale che u.id = Id && u.password = passw)
    @throws NoAccessException se (Exist u appartenente a U tale che u.id = Id && u.password = passw) &&
            Not (Exist (u,d) appartenente a U tale che u.id = Id && u.password = passw && d = file && Access(u,d) = w)
    @modifies this, file
    @effects recupera contenuto di file da file associato
    */
    void readFile(String Id, String passw, E file);
}