package fr.dmconcept.bob.client.communications;

class SendStepResult {

    String mError ;

    public SendStepResult() {
    }

    public void setError(String message) {
       mError = message;
    }

    public boolean isError(){
        return mError != null;
    }

}
