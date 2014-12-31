set src1 = sand-watch.png
set src2 = stopwatch.png

go() {
    pngtopnm $1 | pamscale -xysize 512 512 -filter=sinc | pnmtopng > $1:r-512.png
    pngtopnm $1 | pamscale -xysize 320 320 -filter=sinc | pnmtopng > ../wear/src/main/res/drawable/$1:r-preview.png
    pngtopnm $1 | pamscale -xysize 72 72 -filter=sinc | pnmtopng > ../wear/src/main/res/drawable-hdpi/$1:r-ic_launcher.png
    pngtopnm $1 | pamscale -xysize 48 48 -filter=sinc | pnmtopng > ../wear/src/main/res/drawable-mdpi/$1:r-ic_launcher.png
    pngtopnm $1 | pamscale -xysize 96 96 -filter=sinc | pnmtopng > ../wear/src/main/res/drawable-xhdpi/$1:r-ic_launcher.png
    pngtopnm $1 | pamscale -xysize 144 144 -filter=sinc | pnmtopng > ../wear/src/main/res/drawable-xxhdpi/$1:r-ic_launcher.png
}

go($src1)
go($src2)
