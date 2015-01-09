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

/app -- code that runs on the watch
    src/main/java/org/dwallach/xstopwatch/ -- XStopwatch Java files
