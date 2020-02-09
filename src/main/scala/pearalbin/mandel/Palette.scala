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

trait Palette {
  def colour(iterations: Int): Array[Byte]
}

object Blue256 extends Palette {
  def colour(iterations: Int): Array[Byte] = {
    Array(0.toByte, 0.toByte, (iterations % 256).toByte)
  }
}

object BlueFade extends Palette {
  private val pal = (0 until 256) ++ (0 until 255).reverse
  def colour(iterations: Int): Array[Byte] = {
    Array(0.toByte, 0.toByte, pal(iterations % 511).toByte)
  }
}

object RedFade extends Palette {
  private val pal = (0 until 256) ++ (0 until 255).reverse
  def colour(iterations: Int): Array[Byte] = {
    Array(pal(iterations % 511).toByte, 0.toByte, 0.toByte)
  }
}
