package fr.dmconcept.bob.server.activities

import org.scaloid.common.{STextView, SVerticalLayout, SActivity}

class ClientActivity extends SActivity {

  onCreate {

    contentView = new SVerticalLayout {
      STextView("Client activity")
    }

  }

}
