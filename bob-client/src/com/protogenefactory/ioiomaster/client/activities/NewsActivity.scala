package com.protogenefactory.ioiomaster.client.activities

import org.scaloid.common._

class NewsActivity extends SActivity {

  onCreate {
    contentView {
      new SVerticalLayout {
        STextView("Coming soon")
      }
    }
  }

}
