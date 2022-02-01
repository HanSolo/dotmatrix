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
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoUnit.DAYS;


/**
 * User: hansolo
 * Date: 23.10.17
 * Time: 03:25
 */
public class Demo2 extends Application {
    private static final Random             RND               = new Random();
    private static final DateTimeFormatter  MONTH_FORMATTER   = DateTimeFormatter.ofPattern("MMM");
    private static final DateTimeFormatter  WEEKDAY_FORMATTER = DateTimeFormatter.ofPattern("E");
    private static final DateTimeFormatter  DATE_FORMATTER    = DateTimeFormatter.ofPattern("dd.MM.YYYY");
    private static final long               TOOLTIP_TIMEOUT   = 2000;
    private              GridPane           months;
    private              GridPane           weekdays;
    private              Tooltip            tooltip;
    private              DotMatrix          matrix;
    private              HBox               legend;
    private              Map<Double, Color> colorCoding;
    private              Service            service;
    private              List<Data>         dataList;


    @Override public void init() {
        service = new ProcessService();
        service.setOnSucceeded(e -> {
            tooltip.hide();
            service.reset();
        });

        colorCoding = new ConcurrentSkipListMap<>(Comparator.reverseOrder()); // reverse natural order
        colorCoding.put(8.0, Color.web("#1F6823"));
        colorCoding.put(6.0, Color.web("#45A340"));
        colorCoding.put(4.0, Color.web("#8CC665"));
        colorCoding.put(2.0, Color.web("#D6E685"));
        colorCoding.put(0.0, Color.web("#EEEEEE"));

        LocalDate now         = LocalDate.now();
        int       year        = now.getYear();
        int       month       = now.getMonthValue();
        int       lastDay     = (int) now.range(DAY_OF_MONTH).getMaximum();
        int       daysInMonth = YearMonth.of(year - 1, month).lengthOfMonth();

        LocalDate endDate     = LocalDate.of(year, month, lastDay);
        LocalDate startDate   = endDate.minusYears(1).minusDays(daysInMonth).plusDays(1);
        long      daysInRange = DAYS.between(startDate, endDate);
        int       weeks       = (int) Math.ceil(daysInRange / 7.0);

        months = new GridPane();
        for (int i = 0 ; i < 13 ; i++) {
            Label monthLabel = new Label(MONTH_FORMATTER.format(startDate.plusMonths(i)));
            months.add(monthLabel, i, 0);
            GridPane.setHgrow(monthLabel, Priority.ALWAYS);
            GridPane.setHalignment(monthLabel, HPos.CENTER);
        }

        weekdays = new GridPane();
        for (int i = 0 ; i < 7 ; i++) {
            Label dayLabel = new Label(WEEKDAY_FORMATTER.format(startDate.plusDays(i)));
            dayLabel.setFont(Font.font(6));
            weekdays.add(dayLabel, 0, i);
            GridPane.setVgrow(dayLabel, Priority.ALWAYS);
            GridPane.setValignment(dayLabel, VPos.CENTER);
        }

        tooltip = new Tooltip("");

        matrix = DotMatrixBuilder.create()
                                 .prefSize(600, 80)
                                 .colsAndRows(weeks, 7)
                                 .useSpacer(true)
                                 .spacerSizeFactor(0.1)
                                 .dotShape(DotShape.SQUARE)
                                 .inactiveColor(Color.web("#EEEEEE"))
                                 .build();
        Tooltip.install(matrix, tooltip);

        matrix.setOnDotMatrixEvent(e -> {
            Data selectedData = dataList.stream().filter(data -> data.x == e.getX() && data.y == e.getY()).findFirst().orElse(null);
            StringBuilder tooltipText = new StringBuilder();
            tooltipText.append("Date : ").append(DATE_FORMATTER.format(selectedData.getDate())).append("\n")
                       .append("Value: ").append(null == selectedData ? "-" : String.format(Locale.US, "%.1f", selectedData.getValue()));
            tooltip.setText(tooltipText.toString());
            tooltip.setX(e.getMouseScreenX());
            tooltip.setY(e.getMouseScreenY());
            tooltip.show(matrix.getScene().getWindow());
            if (service.isRunning()) { service.cancel(); service.reset(); }
            service.start();
        });

        legend = new HBox(5,
                          new Text("Less"),
                          new Rectangle(10, 10, colorCoding.get(0.0)),
                          new Rectangle(10, 10, colorCoding.get(2.0)),
                          new Rectangle(10, 10, colorCoding.get(4.0)),
                          new Rectangle(10, 10, colorCoding.get(6.0)),
                          new Rectangle(10, 10, colorCoding.get(8.0)),
                          new Text("More"));
        legend.setAlignment(Pos.CENTER);


        // Set matrix to random data
        dataList = new ArrayList<>(weeks * 7);
        for (int x = 0 ; x < weeks ; x++) {
            for (int y = 0 ; y < 7 ; y++) {
                double value = RND.nextDouble() * 10;
                LocalDate date = startDate.plusDays(x * 7 + y);
                dataList.add(new Data(x, y, value, date));
                matrix.setPixel(x, y, getColor(value));
            }
        }
    }

    private Color getColor(final double value) {
        return colorCoding.get(colorCoding.keySet().stream()
                                          .filter(threshold -> value > threshold)
                                          .findFirst()
                                          .orElse(0.0));
    }

    private void recalcSize(final Scene scene) {
        double width   = scene.getWidth();
        double height  = scene.getHeight();
        double offsetX = (width - matrix.getMatrixWidth()) * 0.5;
        double offsetY = (height - matrix.getMatrixHeight()) * 0.5;

        AnchorPane.setRightAnchor(months, offsetX);
        AnchorPane.setLeftAnchor(months, offsetX);
        AnchorPane.setTopAnchor(months, offsetY - 20);

        AnchorPane.setLeftAnchor(weekdays, offsetX - 10);
        AnchorPane.setTopAnchor(weekdays, offsetY);
        AnchorPane.setBottomAnchor(weekdays, offsetY);

        AnchorPane.setBottomAnchor(legend, offsetY - 20);
    }

    @Override public void start(Stage stage) {
        AnchorPane pane = new AnchorPane(months, weekdays, matrix, legend);
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        AnchorPane.setTopAnchor(months, 10d);
        AnchorPane.setRightAnchor(months, 10d);
        AnchorPane.setLeftAnchor(months, 10d);

        AnchorPane.setTopAnchor(weekdays, 30d);
        AnchorPane.setBottomAnchor(weekdays, 30d);
        AnchorPane.setLeftAnchor(weekdays, 10d);

        AnchorPane.setTopAnchor(matrix, 30d);
        AnchorPane.setRightAnchor(matrix, 10d);
        AnchorPane.setBottomAnchor(matrix, 30d);
        AnchorPane.setLeftAnchor(matrix, 30d);

        AnchorPane.setRightAnchor(legend, 10d);
        AnchorPane.setBottomAnchor(legend, 10d);

        Scene scene = new Scene(pane);
        scene.widthProperty().addListener(o -> recalcSize(scene));
        scene.heightProperty().addListener(o -> recalcSize(scene));

        stage.setTitle("JavaFX DotMatrix Demo");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }


    // ******************** Inner Classes *************************************
    class ProcessService extends Service<Void> {
        @Override protected Task<Void> createTask() {
            return new Task<>() {
                @Override protected Void call() throws Exception {
                    Thread.sleep(TOOLTIP_TIMEOUT);
                    return null;
                }
            };
        }
    }

    class Data {
        private int       x;
        private int       y;
        private double    value;
        private LocalDate date;


        // ******************** Constructors **********************************
        public Data(final int X, final int Y) {
            this(X, Y, 0, LocalDate.now());
        }
        public Data(final int X, final int Y, final double VALUE) {
            this(X, Y, VALUE, LocalDate.now());
        }
        public Data(final int X, final int Y, final double VALUE, final LocalDate DATE) {
            x     = X;
            y     = Y;
            value = VALUE;
            date  = DATE;
        }


        // ******************** Methods ***************************************
        public int getX() { return x; }
        public void setX(final int X) { x = X; }

        public int getY() { return y; }
        public void setY(final int Y) { y = Y; }

        public int[] getXY() { return new int[] {x, y}; }

        public double getValue() { return value; }
        public void setValue(final double VALUE) { value = VALUE; }

        public LocalDate getDate() { return date; }
        public void setDate(final LocalDate DATE) { date = DATE; }
    }
}
