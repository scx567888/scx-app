package dev.scx.app;

public class TTT {

    static void main(String[] args) {
        ScxApp.builder()
            .module(new ScxWebModule())
            .build()
            .run();
    }

}
