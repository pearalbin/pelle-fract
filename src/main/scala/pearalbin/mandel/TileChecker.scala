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

import java.util.concurrent.Executors

import pearalbin.mandel.Mandelbrot.{Region, Tile}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

trait TileChecker {
  def checkTiles(tiles: Iterable[Tile], maxIterations: Int): Iterable[Future[Region]]
}

object SingleThreadTileChecker extends TileChecker {
  private implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newSingleThreadScheduledExecutor())

  def checkTiles(tiles: Iterable[Tile], maxIterations: Int): Iterable[Future[Region]] = {
    tiles.map(tile => Future {
      Mandelbrot.checkTile(tile, maxIterations)
    })
  }
}

class MultiThreadTileChecker(nThreads: Int) extends TileChecker {
  private implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(nThreads))

  def checkTiles(tiles: Iterable[Tile], maxIterations: Int): Iterable[Future[Region]] = {
    tiles.map(tile => Future {
      Mandelbrot.checkTile(tile, maxIterations)
    })
  }
}

class DistributedTileChecker() extends TileChecker {
  override def checkTiles(tiles: Iterable[Tile], maxIterations: Int): Iterable[Future[Region]] = ??? // TODO :)
}
