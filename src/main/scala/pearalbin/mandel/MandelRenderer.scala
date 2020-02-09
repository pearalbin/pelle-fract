// Pelle-Fract - Interactive render of the Mandelbrot set.
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

import javafx.application.{Application, Platform}
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.input.{MouseButton, MouseEvent, ScrollEvent}
import javafx.scene.layout.StackPane
import javafx.scene.{Group, Scene}
import javafx.stage.Stage
import pearalbin.mandel.Mandelbrot.{Region, tiles}

import scala.concurrent.{ExecutionContextExecutor, Future}

case class Box(re0: Double, im0: Double, re1: Double, im1: Double) {
  override def toString: String = s"Box($re0,${im0}i,$re1,${im1}i)"
}

case class MandelState(boundary: Box, width: Int, height: Int, maxIterations: Int, palette: Palette) {
  def modMaxIterations(f: Int => Int): MandelState = {
    this.copy(maxIterations = f(this.maxIterations))
  }

  def center(x: Int, y: Int): MandelState = {
    val scaleRe = (boundary.re1 - boundary.re0) / width
    val scaleIm = (boundary.im1 - boundary.im0) / height
    val reM = boundary.re0 + scaleRe * x
    val imM = boundary.im0 + scaleRe * (height - y)
    val reLength = scaleRe * width
    val imLength = scaleIm * height
    this.copy(boundary = Box(reM - reLength / 2, imM - imLength / 2, reM + reLength / 2, imM + imLength / 2))
  }

  def zoom(f: Double => Double): MandelState = {
    val scaleRe = f((boundary.re1 - boundary.re0) / width)
    val scaleIm = f((boundary.im1 - boundary.im0) / height)
    val reM = (boundary.re0 + boundary.re1) / 2
    val imM = (boundary.im0 + boundary.im1) / 2
    val reLength = scaleRe * width
    val imLength = scaleIm * height
    this.copy(boundary = Box(reM - reLength / 2, imM - imLength / 2, reM + reLength / 2, imM + imLength / 2))
  }
}

object MandelRenderer {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MandelRenderer], args: _*)
  }
}

class MandelRenderer extends Application {
  val width = 1028
  val height = 1028
  private val aspectRatio = 1.0 * width / height

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setTitle("PelleFract")
    val root = new Group()
    val borderPane = new StackPane()
    val canvas = new Canvas(width, height)
    val gc = canvas.getGraphicsContext2D
    borderPane.getChildren.add(canvas)
    root.getChildren.add(borderPane)
    primaryStage.setScene(new Scene(root))
    primaryStage.show()

    var mandelState = MandelState(Box(-2.5, -2 / aspectRatio, 1.5, 2 / aspectRatio), width, height, 512, BlueFade)

    plot()

    canvas.setOnScroll((event: ScrollEvent) => {
      if (event.getDeltaY > 0)
        mandelState = mandelState.modMaxIterations(_ * 2)
      else
        mandelState = mandelState.modMaxIterations(_ / 2)
      plot()
    })

    canvas.setOnMouseClicked((event: MouseEvent) => {
      val x = event.getX
      val y = event.getY
      mandelState = mandelState.center(x.toInt, y.toInt)
      if (event.getButton == MouseButton.PRIMARY) {
        mandelState = mandelState.zoom(_ / 2)
      }
      else if (event.getButton == MouseButton.SECONDARY) {
        mandelState = mandelState.zoom(_ * 2)
      }
      plot()
    })

    def plot(): Unit = {
      println(mandelState)
      plotMandel(gc,
        mandelState.width, mandelState.height,
        mandelState.boundary.re0, mandelState.boundary.im0,
        mandelState.boundary.re1, mandelState.boundary.im1,
        mandelState.maxIterations,
        mandelState.palette)
    }
  }


  def plotMandel(gc: GraphicsContext, width: Int, height: Int, re0: Double, im0: Double, re1: Double, im1: Double, maxIterations: Int, palette: Palette): Unit = {
    val (tileWidth, tileHeight) = (64, 64)
    implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
    Future {
      val ts = tiles(re0, im0, re1, im1, width, height, tileWidth, tileHeight)
      val cores = Runtime.getRuntime.availableProcessors
      val tileChecker = new MultiThreadTileChecker(cores)
      val regions: Iterable[Future[Region]] = tileChecker.checkTiles(ts, maxIterations)
      regions.foreach(fr => {
        fr.foreach(r =>
          Platform.runLater(() => Imaging.plotTile(gc, r.tile.x * tileWidth, height - r.tile.y * tileHeight, r.iterations, palette))
        )
      })
    }
  }
}
