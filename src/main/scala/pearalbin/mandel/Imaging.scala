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

import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelFormat
import pearalbin.mandel.Mandelbrot.Iterations

object Imaging {

  def plotTile(gc: GraphicsContext, x0: Int, y0: Int, iterations: Iterations, palette: Palette): Unit = {
    val pw = gc.getPixelWriter
    val tileWidth = iterations(0).length
    val tileHeight = iterations.length
    val image = iterations.flatten.flatMap(palette.colour)
    pw.setPixels(x0, y0 - tileHeight, tileWidth, tileHeight, PixelFormat.getByteRgbInstance, image, 0, tileWidth * 3)
  }

}
