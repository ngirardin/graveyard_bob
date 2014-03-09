package fr.dmconcept.bob.communications;

import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.Step;

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
