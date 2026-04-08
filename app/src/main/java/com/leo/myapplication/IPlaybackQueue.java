package com.leo.myapplication;

import java.util.List;

/**
 * Interface définissant les actions de navigation dans une file d'attente de lecture musicale.
 */
public interface IPlaybackQueue {
    /**
     * Initialise la file d'attente.
     *
     * @param songs      La liste des musiques.
     * @param startIndex L'index de départ.
     */
    void setQueue(List<Song> songs, int startIndex);

    /** @return Le morceau suivant dans la file. */
    Song getNext();

    /** @return Le morceau précédent dans la file. */
    Song getPrevious();

    /** @return Le morceau actuellement pointé par la file. */
    Song getCurrent();

    /** @return L'index actuel de lecture. */
    int getCurrentIndex();
}