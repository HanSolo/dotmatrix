/*
 * Copyright (c) 2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.dotmatrix;

import javafx.geometry.Point2D;


public class DotMatrixEvent {
    private final int    x;
    private final int    y;
    private final double mouseScreenX;
    private final double mouseScreenY;


    // ******************** Constructors **************************************
    public DotMatrixEvent(final int X, final int Y, final double MOUSE_X, final double MOUSE_Y) {
        x      = X;
        y      = Y;
        mouseScreenX = MOUSE_X;
        mouseScreenY = MOUSE_Y;
    }


    // ******************** Methods *******************************************
    public int getX() { return x; }
    public int getY() { return y; }

    public double getMouseScreenX() { return mouseScreenX; }
    public double getMouseScreenY() { return mouseScreenY; }
    public Point2D getMouseScreenPos() { return new Point2D(mouseScreenX, mouseScreenY); }
}
