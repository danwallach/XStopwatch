#!/bin/tcsh

foreach src (sandwatch_trans.png stopwatch_trans.png) 
    echo -n $src
    convert $src -resize 512x512 $src:r_512.png
    echo -n .
    convert $src -resize 320x320 ../app/src/main/res/drawable/$src:r_preview.png
    echo -n .
    convert $src -resize 72x72 ../app/src/main/res/drawable-hdpi/$src:r_ic_launcher.png
    echo -n .
    convert $src -resize 48x48 ../app/src/main/res/drawable-mdpi/$src:r_ic_launcher.png
    echo -n .
    convert $src -resize 96x96 ../app/src/main/res/drawable-xhdpi/$src:r_ic_launcher.png
    echo -n .
    convert $src -resize 144x144 ../app/src/main/res/drawable-xxhdpi/$src:r_ic_launcher.png
    echo
end
