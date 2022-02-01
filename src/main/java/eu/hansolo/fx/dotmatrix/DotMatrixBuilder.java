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

import eu.hansolo.fx.dotmatrix.DotMatrix.DotShape;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;

import java.util.HashMap;


public class DotMatrixBuilder<B extends DotMatrixBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected DotMatrixBuilder() {}


    // ******************** Methods *******************************************
    public static final DotMatrixBuilder create() {
        return new DotMatrixBuilder();
    }
    
    public final B colsAndRows(final int cols, final int rows) {
        properties.put("cols", new SimpleIntegerProperty(cols));
        properties.put("rows", new SimpleIntegerProperty(rows));
        return (B)this;
    }

    public final B activeColor(final Color color) {
        properties.put("activeColor", new SimpleObjectProperty(color));
        return (B)this;
    }
    public final B inactiveColor(final Color color) {
        properties.put("inactiveColor", new SimpleObjectProperty(color));
        return (B)this;
    }

    public final B dotShape(final DotShape shape) {
        properties.put("dotShape", new SimpleObjectProperty(shape));
        return (B)this;
    }

    public final B matrixFont(final MatrixFont font) {
        properties.put("matrixFont", new SimpleObjectProperty(font));
        return (B)this;
    }

    public final B useSpacer(final boolean useSpacer) {
        properties.put("useSpacer", new SimpleBooleanProperty(useSpacer));
        return (B)this;
    }

    public final B squareDots(final boolean squareDots) {
        properties.put("squareDots", new SimpleBooleanProperty(squareDots));
        return (B)this;
    }

    public final B spacerSizeFactor(final double spacerSizeFactor) {
        properties.put("spacerSizeFactor", new SimpleDoubleProperty(spacerSizeFactor));
        return (B)this;
    }

    public final B prefSize(final double width, final double height) {
        properties.put("prefSize", new SimpleObjectProperty<>(new Dimension2D(width, height)));
        return (B)this;
    }
    public final B minSize(final double width, final double height) {
        properties.put("minSize", new SimpleObjectProperty<>(new Dimension2D(width, height)));
        return (B)this;
    }
    public final B maxSize(final double width, final double height) {
        properties.put("maxSize", new SimpleObjectProperty<>(new Dimension2D(width, height)));
        return (B)this;
    }

    public final B prefWidth(final double prefWidth) {
        properties.put("prefWidth", new SimpleDoubleProperty(prefWidth));
        return (B)this;
    }
    public final B prefHeight(final double prefHeight) {
        properties.put("prefHeight", new SimpleDoubleProperty(prefHeight));
        return (B)this;
    }

    public final B minWidth(final double minWidth) {
        properties.put("minWidth", new SimpleDoubleProperty(minWidth));
        return (B)this;
    }
    public final B minHeight(final double minHeight) {
        properties.put("minHeight", new SimpleDoubleProperty(minHeight));
        return (B)this;
    }

    public final B maxWidth(final double maxWidth) {
        properties.put("maxWidth", new SimpleDoubleProperty(maxWidth));
        return (B)this;
    }
    public final B maxHeight(final double maxHeight) {
        properties.put("maxHeight", new SimpleDoubleProperty(maxHeight));
        return (B)this;
    }

    public final B scaleX(final double scaleX) {
        properties.put("scaleX", new SimpleDoubleProperty(scaleX));
        return (B)this;
    }
    public final B scaleY(final double scaleY) {
        properties.put("scaleY", new SimpleDoubleProperty(scaleY));
        return (B)this;
    }

    public final B layoutX(final double layoutX) {
        properties.put("layoutX", new SimpleDoubleProperty(layoutX));
        return (B)this;
    }
    public final B layoutY(final double layoutY) {
        properties.put("layoutY", new SimpleDoubleProperty(layoutY));
        return (B)this;
    }

    public final B translateX(final double translateX) {
        properties.put("translateX", new SimpleDoubleProperty(translateX));
        return (B)this;
    }
    public final B translateY(final double translateY) {
        properties.put("translateY", new SimpleDoubleProperty(translateY));
        return (B)this;
    }

    public final B padding(final Insets insets) {
        properties.put("padding", new SimpleObjectProperty<>(insets));
        return (B)this;
    }

    public final DotMatrix build() {
        final DotMatrix control;
        if (properties.keySet().contains("cols") && properties.keySet().contains("rows")) {
            int cols = ((IntegerProperty) properties.get("cols")).get();
            int rows = ((IntegerProperty) properties.get("rows")).get();
            control = new DotMatrix(cols, rows);
        } else {
            control = new DotMatrix();
        }

        for (String key : properties.keySet()) {
            if ("prefSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                control.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("minSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                control.setMinSize(dim.getWidth(), dim.getHeight());
            } else if("maxSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                control.setMaxSize(dim.getWidth(), dim.getHeight());
            } else if("prefWidth".equals(key)) {
                control.setPrefWidth(((DoubleProperty) properties.get(key)).get());
            } else if("prefHeight".equals(key)) {
                control.setPrefHeight(((DoubleProperty) properties.get(key)).get());
            } else if("minWidth".equals(key)) {
                control.setMinWidth(((DoubleProperty) properties.get(key)).get());
            } else if("minHeight".equals(key)) {
                control.setMinHeight(((DoubleProperty) properties.get(key)).get());
            } else if("maxWidth".equals(key)) {
                control.setMaxWidth(((DoubleProperty) properties.get(key)).get());
            } else if("maxHeight".equals(key)) {
                control.setMaxHeight(((DoubleProperty) properties.get(key)).get());
            } else if("scaleX".equals(key)) {
                control.setScaleX(((DoubleProperty) properties.get(key)).get());
            } else if("scaleY".equals(key)) {
                control.setScaleY(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutX".equals(key)) {
                control.setLayoutX(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutY".equals(key)) {
                control.setLayoutY(((DoubleProperty) properties.get(key)).get());
            } else if ("translateX".equals(key)) {
                control.setTranslateX(((DoubleProperty) properties.get(key)).get());
            } else if ("translateY".equals(key)) {
                control.setTranslateY(((DoubleProperty) properties.get(key)).get());
            } else if ("padding".equals(key)) {
                control.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
            } else if("activeColor".equals(key)) {
                control.setActiveColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("inactiveColor".equals(key)) {
                control.setInactiveColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("dotShape".equals(key)) {
                control.setDotShape(((ObjectProperty<DotShape>) properties.get(key)).get());
            } else if ("matrixFont".equals(key)) {
                control.setMatrixFont(((ObjectProperty<MatrixFont>) properties.get(key)).get());
            } else if ("useSpacer".equals(key)) {
                control.setUseSpacer(((BooleanProperty) properties.get(key)).get());
            } else if ("spacerSizeFactor".equals(key)) {
                control.setSpacerSizeFactor(((DoubleProperty) properties.get(key)).get());
            } else if ("squareDots".equals(key)) {
                control.setSquareDots(((BooleanProperty) properties.get(key)).get());
            }
        }
        return control;
    }
}