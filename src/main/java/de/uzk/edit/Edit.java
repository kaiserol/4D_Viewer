package de.uzk.edit;

import de.uzk.action.ActionType;

/** Repräsentiert einen einzelnen, umkehrbaren Bildbearbeitungsschritt.
 *  Eine Bearbeitungssitzung, in der nur Operationen vorgenommen werden, die von <code>Edit</code> ableiten,
 *  ist sowohl rekonstruierbar als auch umkehrbar; dies ist von den Implementierungen der abstrakten Methoden für
 *  jede Unterklasse von <code>Edit</code> zu gewährleisten.
 *
 * @see de.uzk.edit.EditManager
 */
public abstract class Edit {
    /** Führt diesen Bearbeitungsschritt durch.
     *  Diese Methode wird sowohl beim ersten Durchführen des Edits aufgerufen als auch bei sämtlichen Wiederholungen
     *  (durch "Redo"). Deshalb sollte sie immer denselben Effekt produzieren. Wenn die Operation (aus welchem Grund auch immer)
     *  im gegebenen Kontext ungültig ist, sollte diese Methode nichts verändern und stattdessen einfach <code>false</code>
     *  zurückgeben. Dieses Ergebnis sollte permanent sein: Gibt die Methode beim ersten Mal <code>false</code> zurück, kann die Operation
     *  aufgegeben werden. Tut sie dies nicht, sollte sie auch bei zukünftigen Aufrufen <code>true</code> zurückgeben.
     *
     * @return <code>true</code>, wenn die Operation durchgeführt wurde, ansonsten <code>false</code>.
     * */
    public abstract boolean perform();

    /** Macht diesen Bearbeitungsschritt rückgängig.
     *  Diese Methode sollte für jeden Aufruf von <code>perform</code> nur ein Mal aufgerufen werden;
     *  was passiert, wenn man (beispielsweise) zweimal hintereinander `undo` aufruft, ist nicht definiert.
     * */
    public abstract void undo();

    /**
     * @return Art des UI-Updates, das durch diese Operation ausgelöst werden soll.
     * Da je nachdem, <i>was</i> überhaupt verändert wurde, unterschiedliche Bereiche der GUI reagieren
     * müssen, bietet diese Methode einen Weg, nicht die gesamte GUI updaten zu müssen.
     *
     * @see de.uzk.action.ActionHandler
     * @see de.uzk.gui.observer.ObserverContainer#handleAction
     * */
    public abstract ActionType getActionType();


}
