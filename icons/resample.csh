#!/bin/tcsh

foreach src (sand-watch.png stopwatch.png) 
    pngtopnm $src | pamscale -xysize 512 512 -filter=sinc | pnmtopng > $src:r-512.png
    pngtopnm $src | pamscale -xysize 320 320 -filter=sinc | pnmtopng > ../app/src/main/res/drawable/$src:r-preview.png
    pngtopnm $src | pamscale -xysize 72 72 -filter=sinc | pnmtopng > ../app/src/main/res/drawable-hdpi/$src:r-ic_launcher.png
    pngtopnm $src | pamscale -xysize 48 48 -filter=sinc | pnmtopng > ../app/src/main/res/drawable-mdpi/$src:r-ic_launcher.png
    pngtopnm $src | pamscale -xysize 96 96 -filter=sinc | pnmtopng > ../app/src/main/res/drawable-xhdpi/$src:r-ic_launcher.png
    pngtopnm $src | pamscale -xysize 144 144 -filter=sinc | pnmtopng > ../app/src/main/res/drawable-xxhdpi/$src:r-ic_launcher.png
end
