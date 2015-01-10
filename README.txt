/*
 * XStopwatch
 * Copyright (C) 2015 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */

What's where:

notes.txt -- ongoing work, notes, to-do items, etc.

/icons -- assorted icons and imagery for XStopwatch
    (note: "resample.csh", at the top level, starts from a
     high-resolution screen dump and generates preview images at all
     the correct resolutions; these downstream dependencies are all
     checked in, so unless you're changing the the icon, you don't
     need to rerun this csh script. Also note that you'll need the
     imagemagick package installed to run it.)

/mobile -- stub for the phone app (no actual code here)

/wear -- code that runs on the watch
    src/main/java/org/dwallach/xstopwatch/ -- XStopwatch Java files

If you're looking to make some other stopwatch or count-down timer
produce broadcast notifications that are compatible with XStopwatch
and XTimer, then you should go have a look at:

PreferencesHelper.java: loads and saves persistent state to the Android
shared preferences and also sends out equivalent state as broadcast intents.
You can use the same action strings (from Constants.java) to send out intents
with the same sorts of values, and compliant watchfaces should render them
properly.

Receiver.java: you don't just have to send out the state of your stopwatch
or timer when it changes, you have to listen for requests to do the same,
which means you need a receiver.

AndroidManifest.xml: you'll see here where we set up the receiver. Also
we have to make sure the service portion (NotificationService.java) starts
up at boot time, so we're ready to respond to these requests.



Also, make sure to verify that XWatchface and CalWatch properly receive
your broadcasts and that you receive theirs.
