module eu.hansolo.fx.dotmatrix {
    // Java
    requires java.base;

    // Java-FX
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    exports eu.hansolo.fx.dotmatrix;
}