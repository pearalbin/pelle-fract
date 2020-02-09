// Pelle-Fract - Interactive renderer of the Mandelbrot set.
// Copyright (C) 2020  Pär A Karlsson
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <https://www.gnu.org/licenses/>.
package pearalbin.mandel

object Mandelbrot {

  case class Tile(re0: Double, im0: Double, re1: Double, im1: Double, x: Int, y: Int, width: Int = 64, height: Int = 64)

  type Iterations = Array[Array[Int]]

  case class Region(tile: Tile, iterations: Iterations)

  @inline
  def checkS(re0: Double, im0: Double, maxIterations: Int): Int = {
    var re = 0.0
    var re2 = re * re
    var im = 0.0
    var im2 = im * im
    var i = 0
    while (re2 + im2 <= 4 & i < maxIterations) {
      val xt = re2 - im2 + re0
      im = 2 * re * im + im0
      re = xt
      re2 = re * re
      im2 = im * im
      i += 1
    }
    i
  }


  def checkRegion0(re0: Double, im0: Double, xStep: Double, yStep: Double, width: Int = 64, height: Int = 64, maxIterations: Int): Iterations = {
    val result = Array.ofDim[Int](height, width)
    for {
      y <- 0 until height
      x <- 0 until width
      re = re0 + x * xStep
      im = im0 + y * yStep
    } yield {
      result(height - 1 - y)(x) = checkS(re, im, maxIterations)
    }
    result
  }

  def checkRegion1(re0: Double, im0: Double, xStep: Double, yStep: Double, width: Int = 64, height: Int = 64, maxIterations: Int): Iterations = {
    var result = Array.ofDim[Int](height, width)
    var xyz = true

    for {
      y <- Seq(0, height - 1)
      im = im0 + y * yStep
      x <- 0 until width
      re = re0 + x * xStep
      iterations = checkS(re, im, maxIterations)
    } yield {
      result(height - 1 - y)(x) = iterations
      xyz = xyz & (iterations == result(0)(0))
    }

    for {
      x <- Seq(0, width - 1)
      re = re0 + x * xStep
      y <- 0 until height
      im = im0 + y * yStep
    } yield {
      val iterations = checkS(re, im, maxIterations)
      result(height - 1 - y)(x) = iterations
      xyz = xyz & (iterations == result(0)(0))
    }

    if (xyz) {
      result = Array.fill(width, height)(result(0)(0))
    } else {
      var y = 1
      var (re, im) = (re0, im0)
      while (y < height - 1) {
        var x = 1
        while (x < width - 1) {
          re = re0 + x * xStep //faster than re += xStep (?)
          im = im0 + y * yStep //faster than im += yStep
          result(height - 1 - y)(x) = checkS(re, im, maxIterations)
          x += 1
        }
        y += 1
      }
      //Above appears to be faster than: TODO Add more structured benchmarking before optimizing any more
      //                  for {
      //                    x <- 1 until width - 1
      //                    y <- 1 until height - 1
      //                    re = re0 + x * xStep
      //                    im = im0 + y * yStep
      //                  } yield {
      //                    result(x)(y) = checkS(re, im, maxIterations)
      //                    //        result(x)(y) = checkJ(re, im, maxIterations)
      //                  }
    }
    result
  }

  def checkTile(tile: Tile, maxIterations: Int): Region = {
    Region(tile, checkRegion1(tile.re0, tile.im0, (tile.re1 - tile.re0) / tile.width, (tile.im1 - tile.im0) / tile.height, tile.width, tile.height, maxIterations))
  }


  def tiles(re0: Double, im0: Double, re1: Double, im1: Double, width: Int, height: Int, tileWidth: Int = 64, tileHeight: Int = 64): Seq[Tile] = {
    val xTiles = width / tileWidth
    val yTiles = height / tileHeight
    val reWidth = re1 - re0
    val resolutionX = reWidth / width
    val imHeight = im1 - im0
    val resolutionY = imHeight / height
    val paddingX = width - xTiles * tileWidth
    val paddingY = height - yTiles * tileHeight

    val tileReWidth = (reWidth - paddingX * resolutionX) / xTiles
    val tileImHeight = (imHeight - paddingY * resolutionY) / yTiles

    val extraX = paddingX match {
      case 0 => Seq.empty
      case _: Int => (0 until yTiles).map(yt =>
        Tile(re0 + xTiles * tileReWidth, im0 + yt * tileImHeight, re1, im0 + (yt + 1) * tileImHeight, xTiles, yt, paddingX, tileHeight)
      )
    }

    val extraY = paddingY match {
      case 0 => Seq.empty
      case _: Int => (0 until xTiles).map(xt =>
        Tile(re0 + xt * tileReWidth, im0 + yTiles * tileImHeight, re0 + (xt + 1) * tileReWidth, im1, xt, yTiles, tileWidth, paddingY)
      )
    }

    val lastTile = if (paddingX > 0 & paddingY > 0)
      Seq(
        Tile(re0 + xTiles * tileReWidth, im0 + yTiles * tileImHeight, re1, im1, xTiles, yTiles, paddingX, paddingY)
      )
    else Seq.empty

    val tiles = for {
      xt <- 0 until xTiles
      yt <- 0 until yTiles
    } yield {
      Tile(re0 + xt * tileReWidth, im0 + yt * tileImHeight, re0 + (xt + 1) * tileReWidth, im0 + (yt + 1) * tileImHeight, xt, yt, tileWidth, tileHeight)
    }

    tiles ++ lastTile ++ extraY ++ extraX
  }
}
