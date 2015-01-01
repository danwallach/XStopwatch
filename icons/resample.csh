#!/bin/tcsh

foreach src (sandwatch.png stopwatch.png) 
    pngtopnm $src | pamscale -xysize 512 512 -filter=sinc | pnmtopng > $src:r_512.png
    pngtopnm $src | pamscale -xysize 320 320 -filter=sinc | pnmtopng > ../app/src/main/res/drawable/$src:r_preview.png
    pngtopnm $src | pamscale -xysize 72 72 -filter=sinc | pnmtopng > ../app/src/main/res/drawable-hdpi/$src:r_ic_launcher.png
    pngtopnm $src | pamscale -xysize 48 48 -filter=sinc | pnmtopng > ../app/src/main/res/drawable-mdpi/$src:r_ic_launcher.png
    pngtopnm $src | pamscale -xysize 96 96 -filter=sinc | pnmtopng > ../app/src/main/res/drawable-xhdpi/$src:r_ic_launcher.png
    pngtopnm $src | pamscale -xysize 144 144 -filter=sinc | pnmtopng > ../app/src/main/res/drawable-xxhdpi/$src:r_ic_launcher.png
end
