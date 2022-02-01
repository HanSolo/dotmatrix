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

import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.concurrent.CopyOnWriteArrayList;


@DefaultProperty("children")
public class DotMatrix extends Region {
    public enum DotShape {ROUND, SQUARE, ROUNDED_RECT}

    public static final  double                                       DEFAULT_SPACER_SIZE_FACTOR = 0.05;
    private static final int                                          RED_MASK                   = 255 << 16;
    private static final int                                          GREEN_MASK                 = 255 << 8;
    private static final int                                          BLUE_MASK                  = 255;
    private static final int                                          ALPHA_MASK                 = 255 << 24;
    private static final double                                       ALPHA_FACTOR               = 1.0 / 255.0;
    private              double                                       preferredWidth;
    private              double                                       preferredHeight;
    private              double                                       width;
    private              double                                       height;
    private              Canvas                                       canvas;
    private              GraphicsContext                              ctx;
    private              StackPane                                    pane;
    private              int                                          activeColor;
    private              int                                          inactiveColor;
    private              DotShape                                     dotShape;
    private              int                                          cols;
    private              int                                          rows;
    private              int[][]                                      matrix;
    private              MatrixFont                                   matrixFont;
    private              int                                          characterWidth;
    private              int                                          characterHeight;
    private              int                                          characterWidthMinusOne;
    private              double                                       dotSize;
    private              double                                       dotWidth;
    private              double                                       dotHeight;
    private              double                                       spacer;
    private              boolean                                      useSpacer;
    private              boolean                                      squareDots;
    private              double                                       spacerSizeFactor;
    private              double                                       dotSizeMinusDoubleSpacer;
    private              double                                       dotWidthMinusDoubleSpacer;
    private              double                                       dotHeightMinusDoubleSpacer;
    private              InvalidationListener                         sizeListener;
    private              EventHandler<MouseEvent>                     clickHandler;
    private              CopyOnWriteArrayList<DotMatrixEventListener> listeners;


    // ******************** Constructors **************************************
    public DotMatrix() {
        this(250, 250, 32, 32, Color.rgb(255, 55, 0), Color.rgb(51, 51, 51, 0.5), DotShape.SQUARE, MatrixFont8x8.INSTANCE);
    }
    public DotMatrix(final int cols, final int rows) {
        this(250, 250, cols, rows, Color.rgb(255, 55, 0), Color.rgb(51, 51, 51, 0.5), DotShape.SQUARE, MatrixFont8x8.INSTANCE);
    }
    public DotMatrix(final int cols, final int rows, final Color dotOnColor) {
        this(250, 250, cols, rows, dotOnColor, Color.rgb(51, 51, 51, 0.5), DotShape.SQUARE, MatrixFont8x8.INSTANCE);
    }
    public DotMatrix(final double preferredWidth, final double preferredHeight, final int cols, final int rows, final Color dotOnColor, final Color dotOffColor, final DotShape dotShape, final MatrixFont matrixFont) {
        this.preferredWidth         = preferredWidth;
        this.preferredHeight        = preferredHeight;
        this.activeColor            = convertToInt(dotOnColor);
        this.inactiveColor          = convertToInt(dotOffColor);
        this.dotShape               = dotShape;
        this.cols                   = cols;
        this.rows                   = rows;
        this.matrixFont             = matrixFont;
        this.matrix                 = new int[this.cols][this.rows];
        this.characterWidth         = this.matrixFont.getCharacterWidth();
        this.characterHeight        = this.matrixFont.getCharacterHeight();
        this.characterWidthMinusOne = characterWidth - 1;
        this.useSpacer              = true;
        this.squareDots             = true;
        this.spacerSizeFactor       = DEFAULT_SPACER_SIZE_FACTOR;
        this.sizeListener           = o -> resize();
        this.clickHandler           = e -> checkForClick(e);
        this.listeners              = new CopyOnWriteArrayList<>();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        // prefill matrix with inactiveColor
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y] = inactiveColor;
            }
        }

        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(preferredWidth, preferredHeight);
            }
        }

        canvas = new Canvas(preferredWidth, preferredHeight);
        ctx = canvas.getGraphicsContext2D();

        pane = new StackPane(canvas);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(sizeListener);
        heightProperty().addListener(sizeListener);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }


    // ******************** Methods *******************************************
    @Override public ObservableList<Node> getChildren() { return super.getChildren(); }

    public void setColsAndRows(final int cols, final int rows) {
        this.cols = cols;
        this.rows = rows;
        matrix    = new int[this.cols][this.rows];
        initGraphics();
        resize();
    }

    public Color getActiveColor() { return convertToColor(activeColor); }
    public void setActiveColor(final Color color) {
        activeColor = convertToInt(color);
        drawMatrix();
    }

    public Color getInactiveColor() { return convertToColor(inactiveColor); }
    public void setInactiveColor(final Color color) {
        inactiveColor = convertToInt(color);
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y] = inactiveColor;
            }
        }
        drawMatrix();
    }

    public DotShape getDotShape() { return dotShape; }
    public void setDotShape(final DotShape dotShape) {
        this.dotShape = dotShape;
        drawMatrix();
    }

    public MatrixFont getMatrixFont() { return matrixFont; }
    public void setMatrixFont(final MatrixFont matrixFont) {
        this.matrixFont             = matrixFont;
        this.characterWidth         = matrixFont.getCharacterWidth();
        this.characterHeight        = matrixFont.getCharacterHeight();
        this.characterWidthMinusOne = characterWidth - 1;
        drawMatrix();
    }

    public boolean isUsingSpacer() { return useSpacer; }
    public void setUseSpacer(final boolean useSpacer) {
        this.useSpacer = useSpacer;
        resize();
    }

    public boolean isSquareDots() { return squareDots; }
    public void setSquareDots(final boolean squareDots) {
        this.squareDots = squareDots;
        resize();
    }

    public double getSpacerSizeFactor() { return spacerSizeFactor; }
    public void setSpacerSizeFactor(final double spacerSizeFactor) {
        this.spacerSizeFactor         = clamp(0.0, 0.2, spacerSizeFactor);
        this.spacer                   = useSpacer ? dotSize * spacerSizeFactor : 0;
        this.dotSizeMinusDoubleSpacer = dotSize - spacer * 2;
        drawMatrix();
    }

    public void setPixel(final int posX, final int posY, final boolean value) { setPixel(posX, posY, value ? activeColor : inactiveColor); }
    public void setPixel(final int posX, final int posY, final Color color) { setPixel(posX, posY, convertToInt(color)); }
    public void setPixel(final int posX, final int posY, final int colorValue) {
        if (posX >= cols || posX < 0) { return; }
        if (posY >= rows || posY < 0) { return; }
        matrix[posX][posY] = colorValue;
    }

    public void setPixelWithRedraw(final int posX, final int posY, final boolean on) {
        setPixel(posX, posY, on ? activeColor : inactiveColor);
        drawMatrix();
    }
    public void setPixelWithRedraw(final int posX, final int posY, final int colorValue) {
        setPixel(posX, posY, colorValue);
        drawMatrix();
    }

    public void setCharAt(final char character, final int posX, final int posY) {
        setCharAt(character, posX, posY, activeColor);
    }
    public void setCharAt(final char character, final int posX, final int posY, final int COLOR_VALUE) {
        int[] c = matrixFont.getCharacter(character);
        for (int x = 0; x < characterWidth; x++) {
            for (int y = 0; y < characterHeight; y++) {
                setPixel(x + posX, y + posY, getBitAt(characterWidthMinusOne - x, y, c) == 0 ? inactiveColor : COLOR_VALUE);
            }
        }
        drawMatrix();
    }

    public void setCharAtWithBackground(final char character, final int posX, final int posY) {
        setCharAtWithBackground(character, posX, posY, activeColor);
    }
    public void setCharAtWithBackground(final char character, final int posX, final int posY, final int colorValue) {
        int[] c = matrixFont.getCharacter(character);
        for (int x = 0; x < characterWidth; x++) {
            for (int y = 0; y < characterHeight; y++) {
                if (getBitAt(characterWidthMinusOne - x, y, c) == 0) continue;
                setPixel(x + posX, y + posY, colorValue);
            }
        }
        drawMatrix();
    }

    public double getDotSize() { return dotSize; }
    public double getDotWidth() { return dotWidth; }
    public double getDotHeight() { return dotHeight; }

    public double getMatrixWidth() { return canvas.getWidth(); }
    public double getMatrixHeight() { return canvas.getHeight(); }

    public Bounds getMatrixLayoutBounds() { return canvas.getLayoutBounds(); }
    public Bounds getMatrixBoundsInParent() { return canvas.getBoundsInParent(); }
    public Bounds getMatrixBoundsInLocal() { return canvas.getBoundsInLocal(); }

    public int getCols() { return cols; }
    public int getRows() { return rows; }

    public int[][] getMatrix() { return matrix; }

    public static Color convertToColor(final int colorValue) {
        return Color.rgb((colorValue & RED_MASK) >> 16, (colorValue & GREEN_MASK) >> 8, (colorValue & BLUE_MASK), ALPHA_FACTOR * ((colorValue & ALPHA_MASK) >>> 24));
    }

    public static int convertToInt(final Color color) {
        return convertToInt((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity());
    }
    public static int convertToInt(final float red, final float green, final float blue, final float alpha) {
        int r = Math.round(255 * red);
        int g = Math.round(255 * green);
        int b = Math.round(255 * blue);
        int a = Math.round(255 * alpha);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getBitAt(final int posX, final int posY, final int[] byteArray) { return (byteArray[posY] >> posX) & 1; }
    public static boolean getBitAtBoolean(final int posX, final int posY, final int[] byteArray) { return ((byteArray[posY] >> posX) & 1) == 1; }

    public int getColorValueAt(final int posX, final int posY) { return matrix[posX][posY]; }

    public Color getColorAt(final int posX, final int posY) { return convertToColor(matrix[posX][posY]); }

    public void shiftLeft() {
        int[] firstColumn = new int[rows];
        for (int y = 0 ; y < rows ; y++) { firstColumn[y] = matrix[0][y]; }
        for (int y = 0 ; y < rows ; y++) {
            for (int x = 1 ; x < cols ; x++) {
                matrix[x - 1][y] = matrix[x][y];
            }
        }
        for (int y = 0 ; y < rows ; y++) { matrix[cols - 1][y] = firstColumn[y]; }
        drawMatrix();
    }
    public void shiftRight() {
        int[] lastColumn = new int[rows];
        for (int y = 0 ; y < rows ; y++) { lastColumn[y] = matrix[cols - 1][y]; }
        for (int y = 0 ; y < rows ; y++) {
            for (int x = cols - 2 ; x >= 0 ; x--) {
                matrix[x + 1][y] = matrix[x][y];
            }
        }
        for (int y = 0 ; y < rows ; y++) { matrix[0][y] = lastColumn[y]; }
        drawMatrix();
    }

    public void shiftUp() {
        int[] firstRow = new int[cols];
        for (int x = 0 ; x < cols ; x++) { firstRow[x] = matrix[x][0]; }
        for (int y = 1 ; y < rows ; y++) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y - 1] = matrix[x][y];
            }
        }
        for (int x = 0 ; x < cols ; x++) { matrix[x][rows - 1] = firstRow[x]; }
        drawMatrix();
    }
    public void shiftDown() {
        int[] lastRow = new int[cols];
        for (int x = 0 ; x < cols ; x++) { lastRow[x] = matrix[x][rows - 1]; }
        for (int y = rows - 2 ; y >= 0 ; y--) {
            for (int x = 0 ; x < cols ; x++) {
                matrix[x][y + 1] = matrix[x][y];
            }
        }
        for (int x = 0 ; x < cols ; x++) { matrix[x][0] = lastRow[x]; }
        drawMatrix();
    }

    public void setAllDotsOn() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                setPixel(x, y, true);
            }
        }
        drawMatrix();
    }
    public void setAllDotsOff() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                setPixel(x, y, false);
            }
        }
        drawMatrix();
    }

    public static final double clamp(final double min, final double max, final double value) {
        if (Double.compare(value, min) < 0) return min;
        if (Double.compare(value, max) > 0) return max;
        return value;
    }

    public void drawMatrix() {
        ctx.clearRect(0, 0, width, height);
        switch(dotShape) {
            case ROUNDED_RECT:
                CtxBounds      bounds      = new CtxBounds(dotWidthMinusDoubleSpacer, dotHeightMinusDoubleSpacer);
                CtxCornerRadii cornerRadii = new CtxCornerRadii(dotSize * 0.125);
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        bounds.setX(x * dotWidth + spacer);
                        bounds.setY(y * dotHeight + spacer);
                        drawRoundedRect(ctx, bounds, cornerRadii);
                        ctx.fill();
                    }
                }
                break;
            case ROUND:
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        ctx.fillOval(x * dotWidth + spacer, y * dotHeight + spacer, dotWidthMinusDoubleSpacer, dotHeightMinusDoubleSpacer);
                    }
                }
                break;
            case SQUARE:
            default    :
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        ctx.setFill(convertToColor(matrix[x][y]));
                        ctx.fillRect(x * dotWidth + spacer, y * dotHeight + spacer, dotWidthMinusDoubleSpacer, dotHeightMinusDoubleSpacer);
                    }
                }
                break;
        }
    }

    public void setOnDotMatrixEvent(final DotMatrixEventListener listener) { addDotMatrixEventListener(listener); }
    public void addDotMatrixEventListener(final DotMatrixEventListener listener) { if (!listeners.contains(listener)) listeners.add(listener); }
    public void removeDotMatrixEventListener(final DotMatrixEventListener listener) { if (listeners.contains(listener)) listeners.remove(listener); }
    public void removeAllDotMatrixEventListeners() { listeners.clear(); }

    public void fireDotMatrixEvent(final DotMatrixEvent evt) {
        for (DotMatrixEventListener listener : listeners) { listener.onDotMatrixEvent(evt); }
    }

    @Override protected double computePrefWidth(final double height) { return super.computePrefWidth(height); }
    @Override protected double computePrefHeight(final double width) { return super.computePrefHeight(width); }

    public void dispose() {
        listeners.clear();
        widthProperty().removeListener(sizeListener);
        heightProperty().removeListener(sizeListener);
        canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, clickHandler);
    }

    private long getRed(final long colorValue) { return  (colorValue & RED_MASK) >> 16; }
    private long getGreen(final long colorValue) { return  (colorValue & GREEN_MASK) >> 8; }
    private long getBlue(final long colorValue) { return (colorValue & BLUE_MASK); }
    private long getAlpha(final long colorValue) { return (colorValue & ALPHA_MASK) >>> 24; }

    private void checkForClick(final MouseEvent evt) {
        double spacerPlusPixelWidthMinusDoubleSpacer  = spacer + dotWidthMinusDoubleSpacer;
        double spacerPlusPixelHeightMinusDoubleSpacer = spacer + dotHeightMinusDoubleSpacer;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (isInRectangle(evt.getX(), evt.getY(), x * dotWidth + spacer, y * dotHeight + spacer, x * dotWidth + spacerPlusPixelWidthMinusDoubleSpacer, y * dotHeight + spacerPlusPixelHeightMinusDoubleSpacer)) {
                    fireDotMatrixEvent(new DotMatrixEvent(x, y, evt.getScreenX(), evt.getScreenY()));
                    break;
                }
            }
        }
    }

    private static void drawRoundedRect(final GraphicsContext ctx, final CtxBounds bounds, final CtxCornerRadii radii) {
        double x           = bounds.getX();
        double y           = bounds.getY();
        double width       = bounds.getWidth();
        double height      = bounds.getHeight();
        double xPlusWidth  = x + width;
        double yPlusHeight = y + height;

        ctx.beginPath();
        ctx.moveTo(x + radii.getTopRight(), y);
        ctx.lineTo(xPlusWidth - radii.getTopRight(), y);
        ctx.quadraticCurveTo(xPlusWidth, y, xPlusWidth, y + radii.getTopRight());
        ctx.lineTo(xPlusWidth, yPlusHeight - radii.getBottomRight());
        ctx.quadraticCurveTo(xPlusWidth, yPlusHeight, xPlusWidth - radii.getBottomRight(), yPlusHeight);
        ctx.lineTo(x + radii.getBottomLeft(), yPlusHeight);
        ctx.quadraticCurveTo(x, yPlusHeight, x, yPlusHeight - radii.getBottomLeft());
        ctx.lineTo(x, y + radii.getTopRight());
        ctx.quadraticCurveTo(x, y, x + radii.getTopRight(), y);
        ctx.closePath();
    }

    private static boolean isInRectangle(final double x, final double y,
                                         final double minX, final double minY,
                                         final double maxX, final double maxY) {
        return (Double.compare(x, minX) >= 0 &&
                Double.compare(x, maxX) <= 0 &&
                Double.compare(y, minY) >= 0 &&
                Double.compare(y, maxY) <= 0);
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width                      = getWidth() - getInsets().getLeft() - getInsets().getRight();
        height                     = getHeight() - getInsets().getTop() - getInsets().getBottom();
        dotSize                    = (width / cols) < (height / rows) ? (width / cols) : (height / rows);
        dotWidth                   = (width / cols);
        dotHeight                  = (height / rows);
        spacer                     = useSpacer ? dotSize * getSpacerSizeFactor() : 0;
        dotSizeMinusDoubleSpacer   = dotSize - spacer * 2;
        dotWidthMinusDoubleSpacer  = dotWidth - spacer * 2;
        dotHeightMinusDoubleSpacer = dotHeight - spacer * 2;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((getWidth() - width) * 0.5, (getHeight() - height) * 0.5);

            if (squareDots) {
                dotWidth                   = dotSize;
                dotHeight                  = dotSize;
                dotWidthMinusDoubleSpacer  = dotSizeMinusDoubleSpacer;
                dotHeightMinusDoubleSpacer = dotSizeMinusDoubleSpacer;
            }
            canvas.setWidth(cols * dotWidth);
            canvas.setHeight(rows * dotHeight);

            drawMatrix();
        }
    }
}
