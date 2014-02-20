package fr.dmconcept.bob;

public interface Observable {
    public void addObserver(Observer obs);
    public void deleteObserver(Observer obs);
}

