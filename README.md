# Pelle-fract
Interactive rendering of the Mandelbrot set. Written in Scala.

##Usage
Mouse:
* Left: zoom in
* Middle: center
* Right: zoom out
* Scroll: change maximum iteration value

##Why
This was originally triggered by yet another discussion on performance of C, C++, Java, and Scala.
From the first thought to revisit some (matrix multiplication) micro benchmarks from a few years ago,
this grow into the idea of doing something more visually attractive.
However, wrapping a first iteration together instantly meant getting lost in surveying the set itself. 

Right now its all Scala, with some selected (possibly pre-mature) optimizations in place.

Time will tell if this evolves to further into surveying (infinite details to be explored), benchmarking (no shortage of things to try there either), or somewhere else.
