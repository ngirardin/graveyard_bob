package fr.dmconcept.bob.client.communications;

import fr.dmconcept.bob.client.models.BoardConfig;
import fr.dmconcept.bob.client.models.Step;

import java.util.ArrayList;

class SendStepInput {

    BoardConfig mBoardConfig;
    ArrayList<Step> mSteps;

    public SendStepInput(BoardConfig boardConfig, ArrayList<Step> steps) {
        mBoardConfig = boardConfig;
        mSteps       = steps;
    }

    public BoardConfig getBoardConfig() {
        return mBoardConfig;
    }

    public ArrayList<Step> getSteps() {
        return mSteps;
    }

}
